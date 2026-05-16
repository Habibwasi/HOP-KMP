"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
require("dotenv/config");
const node_net_1 = require("node:net");
const core_1 = require("@nestjs/core");
const app_module_1 = require("./app.module");
const common_1 = require("@nestjs/common");
const http_exception_filter_1 = require("./common/filters/http-exception.filter");
const transform_interceptor_1 = require("./common/interceptors/transform.interceptor");
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
    const app = await core_1.NestFactory.create(app_module_1.AppModule, { rawBody: true });
    app.useGlobalPipes(new common_1.ValidationPipe({ whitelist: true, transform: true }));
    app.useGlobalFilters(new http_exception_filter_1.HttpExceptionFilter());
    app.useGlobalInterceptors(new transform_interceptor_1.TransformInterceptor());
    app.setGlobalPrefix('api/v1');
    const port = await resolvePort(Number(process.env.PORT ?? 3000));
    await app.listen(port);
    logger.log(`Hop API running on http://localhost:${port}/api/v1`);
}
bootstrap();
//# sourceMappingURL=main.js.map