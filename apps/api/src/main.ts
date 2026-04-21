import { NestFactory } from '@nestjs/core'
import { AppModule } from './app.module'
import { ValidationPipe } from '@nestjs/common'
import { HttpExceptionFilter } from './common/filters/http-exception.filter'

async function bootstrap() {
  const app = await NestFactory.create(AppModule, { rawBody: true })
  app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }))
  app.useGlobalFilters(new HttpExceptionFilter())
  app.setGlobalPrefix('api/v1')
  await app.listen(process.env.PORT ?? 3000)
  console.log(`Hop API running on http://localhost:${process.env.PORT ?? 3000}/api/v1`)
}
bootstrap()
