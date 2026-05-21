import 'dotenv/config'
import './instrument'
import { createServer } from 'node:net'
import { NestFactory } from '@nestjs/core'
import { AppModule } from './app.module'
import { Logger, ValidationPipe } from '@nestjs/common'
import { HttpExceptionFilter } from './common/filters/http-exception.filter'
import { TransformInterceptor } from './common/interceptors/transform.interceptor'

const logger = new Logger('Bootstrap')

function canUsePort(port: number): Promise<boolean> {
  return new Promise((resolve) => {
    const server = createServer()
    server.once('error', () => resolve(false))
    server.once('listening', () => {
      server.close(() => resolve(true))
    })
    server.listen(port)
  })
}

async function resolvePort(preferredPort: number): Promise<number> {
  if (await canUsePort(preferredPort)) return preferredPort

  if (process.env.NODE_ENV === 'production') {
    throw new Error(`Port ${preferredPort} is already in use`)
  }

  for (let port = preferredPort + 1; port <= preferredPort + 20; port += 1) {
    if (await canUsePort(port)) {
      logger.warn(`Port ${preferredPort} is already in use; using ${port} instead`)
      return port
    }
  }

  throw new Error(`No available port found between ${preferredPort} and ${preferredPort + 20}`)
}

async function bootstrap() {
  logger.log(
    `Redis config — URL=${process.env.REDIS_URL ? '[SET]' : '[UNSET]'} HOST=${process.env.REDIS_HOST ?? '[UNSET]'} PORT=${process.env.REDIS_PORT ?? '[UNSET]'} PASSWORD=${process.env.REDIS_PASSWORD ? '[SET]' : '[UNSET]'}`,
  )
  const app = await NestFactory.create(AppModule, { rawBody: true })
  app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }))
  app.useGlobalFilters(new HttpExceptionFilter())
  app.useGlobalInterceptors(new TransformInterceptor())
  app.setGlobalPrefix('api/v1')
  const port = await resolvePort(Number(process.env.PORT ?? 3000))
  await app.listen(port)
  logger.log(`Hop API running on http://localhost:${port}/api/v1`)
}
bootstrap()
