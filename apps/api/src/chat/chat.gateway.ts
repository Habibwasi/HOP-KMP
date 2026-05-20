import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
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
  cors: { origin: '*' },
})
export class ChatGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer() server: Server
  private readonly logger = new Logger(ChatGateway.name)

  constructor(
    @Inject('SUPABASE_CLIENT') private readonly supabase: SupabaseClient,
    private readonly prisma: PrismaService,
    private readonly chatService: ChatService,
  ) {}

  async handleConnection(client: Socket) {
    // Accept token from Socket.io auth payload (Android) or Authorization header (iOS).
    const token =
      (client.handshake.auth as Record<string, string>)?.token ??
      client.handshake.headers?.authorization?.replace(/^Bearer\s+/i, '')

    if (!token) {
      this.logger.warn(`[Chat] Rejected unauthenticated connection ${client.id}`)
      client.disconnect(true)
      return
    }

    const { data: { user }, error } = await this.supabase.auth.getUser(token)
    if (error || !user) {
      this.logger.warn(`[Chat] Auth failed for connection ${client.id}: ${error?.message}`)
      client.disconnect(true)
      return
    }

    const prismaUser = await this.prisma.user.findUnique({ where: { id: user.id } })
    if (!prismaUser) {
      client.disconnect(true)
      return
    }

    const sock = client as AuthenticatedSocket
    sock.userId = prismaUser.id
    sock.userFullName = `${prismaUser.firstName} ${prismaUser.lastName}`.trim()
    this.logger.log(`[Chat] Connected: ${client.id} user=${prismaUser.id}`)
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

    try {
      await this.chatService.assertParticipant(payload.bookingId, sock.userId)
    } catch {
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
