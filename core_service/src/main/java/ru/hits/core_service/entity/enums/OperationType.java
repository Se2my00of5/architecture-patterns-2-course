package ru.hits.core_service.entity.enums;

public enum OperationType {
    // для вкладов
    DEPOSIT,
    WITHDRAWAL,

    // для переводов
    TRANSFER_OUT,
    TRANSFER_IN,

    // для кредитов
    LOAN_DISBURSEMENT,
    LOAN_REPAYMENT
}
