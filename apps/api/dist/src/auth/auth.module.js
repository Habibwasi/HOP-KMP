"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthModule = void 0;
const common_1 = require("@nestjs/common");
const supabase_js_1 = require("@supabase/supabase-js");
const config_1 = require("@nestjs/config");
const prisma_module_1 = require("../prisma/prisma.module");
const supabase_guard_1 = require("./supabase.guard");
let AuthModule = class AuthModule {
};
exports.AuthModule = AuthModule;
exports.AuthModule = AuthModule = __decorate([
    (0, common_1.Global)(),
    (0, common_1.Module)({
        imports: [prisma_module_1.PrismaModule],
        providers: [
            {
                provide: 'SUPABASE_CLIENT',
                inject: [config_1.ConfigService],
                useFactory: (config) => (0, supabase_js_1.createClient)(config.getOrThrow('SUPABASE_URL'), config.getOrThrow('SUPABASE_SERVICE_ROLE_KEY')),
            },
            supabase_guard_1.SupabaseGuard,
        ],
        exports: ['SUPABASE_CLIENT', supabase_guard_1.SupabaseGuard],
    })
], AuthModule);
//# sourceMappingURL=auth.module.js.map