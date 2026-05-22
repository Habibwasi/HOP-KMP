import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common'
import { AppException } from '../../common/errors/app-exception'
import { ApiErrorCode } from '../../common/errors/api-error-codes'

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const req = context.switchToHttp().getRequest()
    if (!req.user?.isAdmin) throw new AppException(ApiErrorCode.ADMIN_REQUIRED)
    return true
  }
}
