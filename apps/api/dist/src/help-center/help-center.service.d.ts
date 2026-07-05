export interface FaqItem {
    id: string;
    topic: string;
    question: string;
    answer: string;
}
export declare class HelpCenterService {
    private readonly faqs;
    getFaqs(): {
        faqs: FaqItem[];
    };
}
