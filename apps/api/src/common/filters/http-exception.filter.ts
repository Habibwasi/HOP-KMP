import { ExceptionFilter, Catch, ArgumentsHost, HttpException, HttpStatus, Logger } from '@nestjs/common'
import { Response } from 'express'
import * as Sentry from '@sentry/nestjs'
import { ApiErrorCode } from '../errors/api-error-codes'

const STATUS_FALLBACK: Partial<Record<number, ApiErrorCode>> = {
  [HttpStatus.BAD_REQUEST]: ApiErrorCode.VALIDATION_ERROR,
  [HttpStatus.UNAUTHORIZED]: ApiErrorCode.UNAUTHORIZED,
  [HttpStatus.FORBIDDEN]: ApiErrorCode.ADMIN_REQUIRED,
  [HttpStatus.NOT_FOUND]: ApiErrorCode.USER_NOT_FOUND,
  [HttpStatus.CONFLICT]: ApiErrorCode.FIELD_TAKEN,
  [HttpStatus.INTERNAL_SERVER_ERROR]: ApiErrorCode.INTERNAL_ERROR,
}

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(HttpExceptionFilter.name)

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp()
    const response = ctx.getResponse<Response>()

    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR

    if (!(exception instanceof HttpException)) {
      this.logger.error('Unhandled exception', exception instanceof Error ? exception.stack : String(exception))
      Sentry.captureException(exception)
    }

    const exceptionResponse =
      exception instanceof HttpException ? exception.getResponse() : null

    const responseObj =
      typeof exceptionResponse === 'object' && exceptionResponse !== null
        ? (exceptionResponse as Record<string, any>)
        : null

    const errorCode: string =
      responseObj?.errorCode ??
      STATUS_FALLBACK[status] ??
      ApiErrorCode.INTERNAL_ERROR

    const rawMessage =
      responseObj?.message ??
      (exception instanceof Error ? exception.message : 'Internal server error')
    const message = Array.isArray(rawMessage) ? rawMessage.join(', ') : (rawMessage as string)

    const details: Array<{ field: string; message: string }> | null =
      responseObj?.details ?? null

    response.status(status).json({
      error: {
        statusCode: status,
        errorCode,
        message,
        details,
      },
    })
  }
}
