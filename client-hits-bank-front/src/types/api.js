
export const UserRoles = {
  CLIENT: 'CLIENT',
  EMPLOYEE: 'EMPLOYEE'
};

/**
 * @typedef {Object} User
 * @property {string} id
 * @property {string} login
 * @property {string} fullName
 * @property {'CLIENT' | 'EMPLOYEE'} role
 * @property {boolean} isBlocked
 * @property {string} createdAt
 * @property {string} updatedAt
 */

/**
 * @typedef {Object} CreateUserRequest
 * @property {string} login
 * @property {string} password
 * @property {string} fullName
 * @property {('CLIENT' | 'EMPLOYEE')[]} roles
 */

/**
 * @typedef {Object} CreateUserResponse
 * @property {string} id
 * @property {string} login
 * @property {string} fullName
 * @property {('CLIENT' | 'EMPLOYEE')[]} roles
 * @property {boolean} isBlocked
 * @property {string} createdAt
 */


/**
 * @typedef {Object} Account
 * @property {string} id
 * @property {string} userId
 * @property {number} balance
 * @property {'RUB' | 'USD' | 'CNY'} currency
 * @property {'ACTIVE' | 'CLOSED'} status
 * @property {string} createdAt
 * @property {string | null} closedAt
 */

/**
 * @typedef {Object} CreateAccountRequest
 * @property {string} userId
 * @property {string} currency
 */

/**
 * @typedef {Object} DepositRequest
 * @property {number} amount
 * @property {string} [description]
 */

/**
 * @typedef {Object} WithdrawRequest
 * @property {number} amount
 * @property {string} [description]
 */

/**
 * @typedef {Object} TransferRequest
 * @property {string} targetAccountId
 * @property {number} amount
 * @property {string} [description]
 */


export const OperationTypes = {
  DEPOSIT: 'DEPOSIT',
  WITHDRAWAL: 'WITHDRAWAL',
  TRANSFER_OUT: 'TRANSFER_OUT',
  TRANSFER_IN: 'TRANSFER_IN',
  LOAN_DISBURSEMENT: 'LOAN_DISBURSEMENT',
  LOAN_REPAYMENT: 'LOAN_REPAYMENT'
};

/**
 * @typedef {Object} Operation
 * @property {string} id
 * @property {string} accountId
 * @property {keyof typeof OperationTypes} type
 * @property {number} amount
 * @property {string} createdAt
 * @property {string | null} description
 * @property {string | null} creditId
 */

export const CreditStatuses = {
  ACTIVE: 'Active',
  PAID: 'Paid',
  OVERDUE: 'Overdue',
  DEFAULTED: 'Defaulted'
};

/**
 * @typedef {Object} Tariff
 * @property {string} id
 * @property {string} name
 * @property {number} interestRate
 * @property {boolean} isActive
 * @property {string} createdAt
 * @property {string | null} updatedAt
 */

/**
 * @typedef {Object} CreateTariffRequest
 * @property {string} name
 * @property {number} interestRate
 */

/**
 * @typedef {Object} ApplyCreditRequest
 * @property {string} clientId
 * @property {string} accountId
 * @property {string} tariffId
 * @property {number} amount
 * @property {number} termInMonths
 */

/**
 * @typedef {Object} Credit
 * @property {string} id
 * @property {string} clientId
 * @property {string} accountId
 * @property {string} tariffName
 * @property {number} interestRate
 * @property {number} amount
 * @property {number} remainingAmount
 * @property {number} monthlyPayment
 * @property {string} startDate
 * @property {string | null} endDate
 * @property {keyof typeof CreditStatuses} status
 * @property {number} paymentsCount
 * @property {number} totalPaid
 */

/**
 * @typedef {Object} CreditRating
 * @property {string} clientId
 * @property {number} score
 * @property {'A' | 'B' | 'C' | 'D' | 'F'} grade
 * @property {number} totalCredits
 * @property {number} paidCredits
 * @property {number} overduePayments
 * @property {number} onTimePaymentRate
 */

/**
 * @typedef {Object} MakePaymentRequest
 * @property {string} creditId
 * @property {string} accountId
 * @property {number} amount
 */

/**
 * @typedef {Object} OAuth2TokenResponse
 * @property {string} access_token
 * @property {string} refresh_token
 * @property {number} expires_in
 * @property {string} token_type
 * @property {string} scope
 */

/**
 * @typedef {Object} OAuth2UserInfo
 * @property {string} user_id
 * @property {string} login
 * @property {string} full_name
 * @property {string} roles
 */

/**
 * @typedef {Object} OAuth2Tokens
 * @property {string} accessToken
 * @property {string} refreshToken
 * @property {number} expiresAt
 * @property {string} tokenType
 */

/**
 * @typedef {Object} OAuth2User
 * @property {string} id
 * @property {string} login
 * @property {string} fullName
 * @property {string} roles
 */

/**
 * @typedef {Object} WebSocketOperation
 * @property {string} id
 * @property {string} accountId
 * @property {keyof typeof OperationTypes} type
 * @property {number} amount
 * @property {string} createdAt
 * @property {string | null} description
 * @property {string | null} creditId
 */

/**
 * @typedef {Object} WebSocketSubscribeOptions
 * @property {string} Authorization
 */

/**
 * @template T
 * @typedef {Object} ApiResponse
 * @property {boolean} success
 * @property {T} [data]
 * @property {string} [error]
 * @property {number} [status]
 */

/**
 * @typedef {Object} ApiError
 * @property {string} message
 * @property {number} [status]
 * @property {any} [data]
 */