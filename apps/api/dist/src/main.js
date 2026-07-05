"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
require("dotenv/config");
require("./instrument");
const node_net_1 = require("node:net");
const core_1 = require("@nestjs/core");
const app_module_1 = require("./app.module");
const common_1 = require("@nestjs/common");
const http_exception_filter_1 = require("./common/filters/http-exception.filter");
const transform_interceptor_1 = require("./common/interceptors/transform.interceptor");
const app_exception_1 = require("./common/errors/app-exception");
const api_error_codes_1 = require("./common/errors/api-error-codes");
const logger = new common_1.Logger('Bootstrap');
function canUsePort(port) {
    return new Promise((resolve) => {
        const server = (0, node_net_1.createServer)();
        server.once('error', () => resolve(false));
        server.once('listening', () => {
            server.close(() => resolve(true));
        });
        server.listen(port);
    });
}
async function resolvePort(preferredPort) {
    if (await canUsePort(preferredPort))
        return preferredPort;
    if (process.env.NODE_ENV === 'production') {
        throw new Error(`Port ${preferredPort} is already in use`);
    }
    for (let port = preferredPort + 1; port <= preferredPort + 20; port += 1) {
        if (await canUsePort(port)) {
            logger.warn(`Port ${preferredPort} is already in use; using ${port} instead`);
            return port;
        }
    }
    throw new Error(`No available port found between ${preferredPort} and ${preferredPort + 20}`);
}
async function bootstrap() {
    logger.log(`Redis config — URL=${process.env.REDIS_URL ? '[SET]' : '[UNSET]'} HOST=${process.env.REDIS_HOST ?? '[UNSET]'} PORT=${process.env.REDIS_PORT ?? '[UNSET]'} PASSWORD=${process.env.REDIS_PASSWORD ? '[SET]' : '[UNSET]'}`);
    const app = await core_1.NestFactory.create(app_module_1.AppModule, { rawBody: true });
    app.useGlobalPipes(new common_1.ValidationPipe({
        whitelist: true,
        transform: true,
        exceptionFactory: (errors) => {
            const details = errors.flatMap((e) => Object.values(e.constraints ?? {}).map((msg) => ({ field: e.property, message: msg })));
            return new app_exception_1.AppException(api_error_codes_1.ApiErrorCode.VALIDATION_ERROR, undefined, details);
        },
    }));
    app.useGlobalFilters(new http_exception_filter_1.HttpExceptionFilter());
    app.useGlobalInterceptors(new transform_interceptor_1.TransformInterceptor());
    app.setGlobalPrefix('api/v1', { exclude: ['privacy'] });
    const port = await resolvePort(Number(process.env.PORT ?? 3000));
    await app.listen(port);
    logger.log(`Hop API running on http://localhost:${port}/api/v1`);
}
bootstrap();
//# sourceMappingURL=main.js.map