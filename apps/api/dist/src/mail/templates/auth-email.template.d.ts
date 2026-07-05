export type EmailActionType = 'signup' | 'recovery' | 'magic_link' | 'invite' | 'email_change' | 'email_otp';
export interface AuthEmailContext {
    actionType: EmailActionType;
    recipientEmail: string;
    token: string;
    tokenHash: string;
    redirectTo: string;
    siteUrl: string;
    apiBaseUrl: string;
}
export declare function buildSubject(actionType: EmailActionType): string;
export declare function buildAuthEmail(ctx: AuthEmailContext): string;
