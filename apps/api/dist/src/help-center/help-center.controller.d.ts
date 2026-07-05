import { HelpCenterService } from './help-center.service';
export declare class HelpCenterController {
    private readonly helpCenter;
    constructor(helpCenter: HelpCenterService);
    getFaqs(): {
        faqs: import("./help-center.service").FaqItem[];
    };
}
