package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.*;
import com.smartcon.global.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 사용자 정보 암호화 서비스
 * PersonalInfo, BusinessInfo, BankAccount의 민감 정보 암호화/복호화 담당
 */
@Service
@RequiredArgsConstructor
public class UserEncryptionService {

    private final EncryptionService encryptionService;

    /**
     * 주민번호 암호화 및 PersonalInfo 설정
     * @param personalInfo PersonalInfo 객체
     * @param ssn 평문 주민번호
     */
    public void encryptAndSetSsn(PersonalInfo personalInfo, String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            return;
        }

        String encrypted = encryptionService.encrypt(ssn);
        personalInfo.setSsn(ssn, encrypted);
    }

    /**
     * 주민번호 복호화
     * @param personalInfo PersonalInfo 객체
     * @return 복호화된 주민번호
     */
    public String decryptSsn(PersonalInfo personalInfo) {
        if (personalInfo == null || personalInfo.getEncryptedSsn() == null) {
            return null;
        }

        return encryptionService.decrypt(personalInfo.getEncryptedSsn());
    }

    /**
     * 마스킹된 주민번호 반환
     * @param personalInfo PersonalInfo 객체
     * @return 마스킹된 주민번호
     */
    public String getMaskedSsn(PersonalInfo personalInfo) {
        if (personalInfo == null) {
            return null;
        }

        String decrypted = decryptSsn(personalInfo);
        if (decrypted == null) {
            return personalInfo.getMaskedSsn();
        }

        return encryptionService.maskSsn(decrypted);
    }

    /**
     * 사업자번호 암호화 및 BusinessInfo 설정
     * @param businessInfo BusinessInfo 객체
     * @param businessNumber 평문 사업자번호
     */
    public void encryptAndSetBusinessNumber(BusinessInfo businessInfo, String businessNumber) {
        if (businessNumber == null || businessNumber.trim().isEmpty()) {
            return;
        }

        String encrypted = encryptionService.encrypt(businessNumber);
        businessInfo.setBusinessNumber(businessNumber, encrypted);
    }

    /**
     * 사업자번호 복호화
     * @param businessInfo BusinessInfo 객체
     * @return 복호화된 사업자번호
     */
    public String decryptBusinessNumber(BusinessInfo businessInfo) {
        if (businessInfo == null || businessInfo.getEncryptedBusinessNumber() == null) {
            return null;
        }

        return encryptionService.decrypt(businessInfo.getEncryptedBusinessNumber());
    }

    /**
     * 마스킹된 사업자번호 반환
     * @param businessInfo BusinessInfo 객체
     * @return 마스킹된 사업자번호
     */
    public String getMaskedBusinessNumber(BusinessInfo businessInfo) {
        if (businessInfo == null) {
            return null;
        }

        String decrypted = decryptBusinessNumber(businessInfo);
        if (decrypted == null) {
            return businessInfo.getMaskedBusinessNumber();
        }

        return encryptionService.maskBusinessNumber(decrypted);
    }

    /**
     * 계좌번호 암호화 및 BankAccount 설정
     * @param bankAccount BankAccount 객체
     * @param accountNumber 평문 계좌번호
     */
    public void encryptAndSetAccountNumber(BankAccount bankAccount, String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return;
        }

        String encrypted = encryptionService.encrypt(accountNumber);
        bankAccount.setAccountNumber(accountNumber, encrypted);
    }

    /**
     * 계좌번호 복호화
     * @param bankAccount BankAccount 객체
     * @return 복호화된 계좌번호
     */
    public String decryptAccountNumber(BankAccount bankAccount) {
        if (bankAccount == null || bankAccount.getEncryptedAccountNumber() == null) {
            return null;
        }

        return encryptionService.decrypt(bankAccount.getEncryptedAccountNumber());
    }

    /**
     * 마스킹된 계좌번호 반환
     * @param bankAccount BankAccount 객체
     * @return 마스킹된 계좌번호
     */
    public String getMaskedAccountNumber(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        String decrypted = decryptAccountNumber(bankAccount);
        if (decrypted == null) {
            return bankAccount.getMaskedAccountNumber();
        }

        return encryptionService.maskAccountNumber(decrypted);
    }

    /**
     * 휴대폰 번호 암호화
     * @param phoneNumber 평문 휴대폰 번호
     * @return 암호화된 휴대폰 번호
     */
    public String encryptPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        return encryptionService.encrypt(phoneNumber);
    }

    /**
     * 휴대폰 번호 복호화
     * @param encryptedPhoneNumber 암호화된 휴대폰 번호
     * @return 복호화된 휴대폰 번호
     */
    public String decryptPhoneNumber(String encryptedPhoneNumber) {
        if (encryptedPhoneNumber == null || encryptedPhoneNumber.trim().isEmpty()) {
            return null;
        }

        return encryptionService.decrypt(encryptedPhoneNumber);
    }

    /**
     * 마스킹된 휴대폰 번호 반환
     * @param phoneNumber 평문 휴대폰 번호
     * @return 마스킹된 휴대폰 번호
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        return encryptionService.maskPhoneNumber(phoneNumber);
    }
}
