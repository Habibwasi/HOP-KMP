import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
  OnGatewayInit,
  MessageBody,
  ConnectedSocket,
  WsException,
} from '@nestjs/websockets'
import { Inject, Logger } from '@nestjs/common'
import { Server, Socket } from 'socket.io'
import { SupabaseClient } from '@supabase/supabase-js'
import { PrismaService } from '../prisma/prisma.service'
import { ChatService } from './chat.service'

interface AuthenticatedSocket extends Socket {
  userId: string
  userFullName: string
}

@WebSocketGateway({
  namespace: '/chat',
  transports: ['websocket'],
  // Restrict allowed origins via env var; falls back to '*' for local dev only.
  // Mobile clients (iOS/Android) do not send an Origin header, so they are
  // unaffected by CORS restrictions regardless of this setting.
  cors: { origin: process.env.ALLOWED_ORIGINS?.split(',').map((o) => o.trim()) ?? '*' },
})
export class ChatGateway implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer() server: Server
  private readonly logger = new Logger(ChatGateway.name)

  constructor(
    @Inject('SUPABASE_CLIENT') private readonly supabase: SupabaseClient,
    private readonly prisma: PrismaService,
    private readonly chatService: ChatService,
  ) {}

  /**
   * Register auth middleware that runs BEFORE the connect ack is sent to the
   * client.  This eliminates the race condition where handleConnection is async
   * (Supabase + Prisma calls) and the client fires "join" before sock.userId
   * has been set.  With middleware, the connect ack is only sent after next()
   * is called, so sock.userId is always ready when any subsequent event arrives.
   */
  afterInit(server: Server) {
    server.use(async (socket, next) => {
      const token =
        (socket.handshake.auth as Record<string, string>)?.token ??
        socket.handshake.headers?.authorization?.replace(/^Bearer\s+/i, '')

      if (!token) {
        this.logger.warn(`[Chat] Rejected unauthenticated connection ${socket.id}`)
        return next(new Error('Unauthorized'))
      }

      const { data: { user }, error } = await this.supabase.auth.getUser(token)
      if (error || !user) {
        this.logger.warn(`[Chat] Auth failed for connection ${socket.id}: ${error?.message}`)
        return next(new Error('Unauthorized'))
      }

      const prismaUser = await this.prisma.user.findUnique({ where: { id: user.id } })
      if (!prismaUser) {
        return next(new Error('Unauthorized'))
      }

      const sock = socket as AuthenticatedSocket
      sock.userId = prismaUser.id
      sock.userFullName = `${prismaUser.firstName} ${prismaUser.lastName}`.trim()
      next()
    })
  }

  handleConnection(client: Socket) {
    const sock = client as AuthenticatedSocket
    this.logger.log(`[Chat] Connected: ${client.id} user=${sock.userId}`)
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`[Chat] Disconnected: ${client.id}`)
  }

  @SubscribeMessage('join')
  async handleJoin(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { bookingId: string },
  ) {
    const sock = client as AuthenticatedSocket
    if (!sock.userId) throw new WsException('Unauthorized')

    try {
      await this.chatService.assertParticipant(payload.bookingId, sock.userId)
    } catch {
      throw new WsException('Not a participant of this booking')
    }

    await client.join(`booking:${payload.bookingId}`)
    this.logger.log(`[Chat] ${sock.userId} joined booking:${payload.bookingId}`)

    // Push the last 50 messages directly so the client doesn't need a separate
    // ack-based 'history' request (simpler for both Android and iOS clients).
    const history = await this.chatService.getHistory(payload.bookingId, sock.userId)
    client.emit('history', history)

    return { joined: payload.bookingId }
  }

  @SubscribeMessage('message')
  async handleMessage(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { bookingId: string; body: string },
  ) {
    const sock = client as AuthenticatedSocket
    if (!sock.userId) throw new WsException('Unauthorized')
    if (!payload.body?.trim()) throw new WsException('Empty message')

    // Verify the sender has already joined the booking room.  Room membership
    // is the server-side proof of the assertParticipant check that ran during
    // handleJoin — no extra DB round-trip needed per message.
    if (!client.rooms.has(`booking:${payload.bookingId}`)) {
      throw new WsException('Not a participant of this booking')
    }

    const message = await this.chatService.createMessage(
      payload.bookingId,
      sock.userId,
      sock.userFullName,
      payload.body.trim(),
    )

    // Broadcast to all OTHER participants in the room, then echo directly back
    // to the sender.  Using client.to() + client.emit() (rather than
    // server.to()) guarantees the sender always receives their own message
    // even if there is a brief window between the join and the send.
    client.to(`booking:${payload.bookingId}`).emit('message', message)
    client.emit('message', message)
    return { sent: true }
  }

  @SubscribeMessage('history')
  async handleHistory(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { bookingId: string },
  ) {
    const sock = client as AuthenticatedSocket
    if (!sock.userId) throw new WsException('Unauthorized')

    const messages = await this.chatService.getHistory(payload.bookingId, sock.userId)
    return { messages }
  }
}
