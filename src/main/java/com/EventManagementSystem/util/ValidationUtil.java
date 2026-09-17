package com.EventManagementSystem.util;

import com.EventManagementSystem.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.time.LocalDateTime;

@Component
public class ValidationUtil {

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+._-]+@[A-Za-z0-9._+-]+\\.[A-Za-z]{2,}$");

    public static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z ]+(?: [A-Za-z])*$");

    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$&*]).{6,}$");

    public static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNo(String ph_no) {
        if (isEmpty(ph_no)) {
            return false;
        }
        return PHONE_PATTERN.matcher(ph_no).matches();
    }

    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidName(String name) {
        if (isEmpty(name)) {
            return false;
        }

        return NAME_PATTERN.matcher(name).matches();
    }

    public void validateName(String name) {
        if (ValidationUtil.isEmpty(name)) {
            throw new ValidationException("Name Cannot be Null");
        } else if (!ValidationUtil.isValidName(name)) {
            throw new ValidationException("Name cannot have a special character or numbers or unwanted space");
        }
    }

    public void validateEmail(String email) {
        if (ValidationUtil.isEmpty(email)) {
            throw new ValidationException("Email Cannot be empty");
        } else if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Wrong Email Format");
        }
    }

    public void validatePassword(String password) {
        if (ValidationUtil.isEmpty(password)) {
            throw new ValidationException("Password Cannot be empty");
        } else if (password.length() < 8) {
            throw new ValidationException("Password should contain atleast of 8 characters");
        } else if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password should contain a small letter,"
                    + " capital letter, number and a special character");
        }
    }

    public void validatePhone(String phone) {
        if (ValidationUtil.isEmpty(phone)) {
            throw new ValidationException("Phone number cannot be empty");
        } else if (!ValidationUtil.isValidPhoneNo(phone)) {
            throw new ValidationException("Phone should be in 10 digit number");
        }
    }

}
