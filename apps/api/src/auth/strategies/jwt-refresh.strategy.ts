import { Injectable, UnauthorizedException } from '@nestjs/common'
import { PassportStrategy } from '@nestjs/passport'
import { ExtractJwt, Strategy, StrategyOptionsWithRequest } from 'passport-jwt'
import { ConfigService } from '@nestjs/config'
import { Request } from 'express'

@Injectable()
export class JwtRefreshStrategy extends PassportStrategy(Strategy, 'jwt-refresh') {
  constructor(private config: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromBodyField('refreshToken'),
      secretOrKey: config.getOrThrow<string>('JWT_REFRESH_SECRET'),
      passReqToCallback: true,
    } as StrategyOptionsWithRequest)
  }

  async validate(req: Request, payload: { sub: string }) {
    const refreshToken = req.body?.refreshToken
    if (!refreshToken) throw new UnauthorizedException()
    return { ...payload, refreshToken }
  }
}
