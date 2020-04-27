package com.orange.config;

public class AppConstants {

	// regex for transaction validations
	public static final String DESCRIPTION_REGEX = "^[1-9a-zA-Z ,.-]{1,30}$";
	public static final String AMOUNT_REGEX = "^[\\d]+[\\.][\\d]{2}|[\\d]+$";

	// regex for client validations
	public static final String WALLET_REGEX = "^(\\+4|)?(07[0-8]{1}[0-9]{1}|02[0-9]{2}|03[0-9]{2}){1}?(\\s|\\.|\\-)?([0-9]{3}(\\s|\\.|\\-|)){2}$";
	public static final String IBAN_REGEX = "\\bRO\\d{2}[A-Z]{4}\\d{16}\\b";
	public static final String NAME_REGEX = "^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$";
	public static final String CNP_REGEX = "\\b[1-8]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])(0[1-9]|[1-4]\\d|5[0-2]|99)\\d{4}$";

}
