/**
 * Danish tax authority (SKAT) reimbursement rate for carpooling.
 * Updated annually — change this constant to apply project-wide.
 * Override via the SKAT_RATE_DKK_PER_KM environment variable for deployments.
 */
export const SKAT_RATE_DKK_PER_KM: number =
  parseFloat(process.env.SKAT_RATE_DKK_PER_KM ?? '') || 0.27
