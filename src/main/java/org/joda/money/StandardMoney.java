/*
 *  Copyright 2009-2010 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount of money with the standard decimal places defined by the currency.
 * <p>
 * This class represents a quantity of money, stored as a {@code BigDecimal} amount
 * in a single {@link CurrencyUnit currency}.
 * <p>
 * Every currency has a certain standard number of decimal places.
 * This is typically 2 (Euro, British Pound, US Dollar) but might be
 * 0 (Japanese Yen), 1 (Vietnamese Dong) or 3 (Bahrain Dinar).
 * The {@code StandardMoney} class is fixed to this number of decimal places.
 * <p>
 * For example, US dollars has a standard number of decimal places of 2.
 * The major units are dollars. The minor units are cents, 100 to the dollar.
 * This class does not allow calculations on fractions of a cent.
 * <p>
 * StandardMoney is immutable and thread-safe.
 */
public final class StandardMoney implements MoneyProvider, Comparable<MoneyProvider>, Serializable {

    /**
     * The serialisation version.
     */
    private static final long serialVersionUID = 723473581L;

    /**
     * The money, not null.
     */
    private final Money iMoney;

    //-----------------------------------------------------------------------
    /**
     * Gets an instance of {@code StandardMoney} in the specified currency.
     * <p>
     * This allows you to create an instance with a specific currency and amount.
     * No rounding is performed on the amount, so it must have a scale compatible
     * with the currency.
     *
     * @param currency  the currency, not null
     * @param amount  the amount of money, not null
     * @return the new instance, never null
     * @throws ArithmeticException if the scale exceeds the currency scale
     */
    public static StandardMoney of(CurrencyUnit currency, BigDecimal amount) {
        MoneyUtils.checkNotNull(currency, "Currency must not be null");
        if (amount.scale() > currency.getDecimalPlaces()) {
            throw new ArithmeticException("Scale of amount " + amount + " is greater than the scale of the currency " + currency);
        }
        return StandardMoney.of(currency, amount, RoundingMode.UNNECESSARY);
    }

    /**
     * Gets an instance of {@code StandardMoney} in the specified currency, rounding as necessary.
     * <p>
     * This allows you to create an instance with a specific currency and amount.
     * If the amount has a scale in excess of the scale of the currency then the excess
     * fractional digits are rounded using the rounding mode.
     *
     * @param currency  the currency, not null
     * @param amount  the amount of money, not null
     * @param roundingMode  the rounding mode to use, not null
     * @return the new instance, never null
     * @throws ArithmeticException if the rounding fails
     */
    public static StandardMoney of(CurrencyUnit currency, BigDecimal amount, RoundingMode roundingMode) {
        return new StandardMoney(Money.ofCurrencyScale(currency, amount, roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an instance of {@code StandardMoney} in the specified currency.
     * <p>
     * This allows you to create an instance with a specific currency and amount.
     * The amount is a whole number only. Thus you can initialise the value
     * 'USD 20', but not the value 'USD 20.32'.
     * <p>
     * For example, {@code ofMajor(USD, 25)} creates the instance {@code USD 25.00}.
     *
     * @param currency  the currency, not null
     * @param amountMajor  the amount of money in the major division of the currency
     * @return the new instance, never null
     * @throws ArithmeticException if the amount is too large
     */
    public static StandardMoney ofMajor(CurrencyUnit currency, long amountMajor) {
        return StandardMoney.of(currency, BigDecimal.valueOf(amountMajor), RoundingMode.UNNECESSARY);
    }

    /**
     * Gets an instance of {@code StandardMoney} in the specified currency.
     * <p>
     * This allows you to create an instance with a specific currency and amount
     * expressed in terms of the minor unit.
     * For example, if constructing US Dollars, the input to this method represents cents.
     * Note that when a currency has zero decimal places, the major and minor units are the same.
     * <p>
     * For example, {@code ofMajor(USD, 2595)} creates the instance {@code USD 25.95}.
     *
     * @param currency  the currency, not null
     * @param amountMinor  the amount of money in the minor division of the currency
     * @return the new instance, never null
     */
    public static StandardMoney ofMinor(CurrencyUnit currency, long amountMinor) {
        return new StandardMoney(Money.ofMinor(currency, amountMinor));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an instance of {@code StandardMoney} representing zero in the specified currency.
     * <p>
     * For example, {@code zero(USD)} creates the instance {@code USD 0.00}.
     *
     * @param currency  the currency, not null
     * @return the instance representing zero, never null
     */
    public static StandardMoney zero(CurrencyUnit currency) {
        MoneyUtils.checkNotNull(currency, "Currency must not be null");
        BigDecimal bd = BigDecimal.valueOf(0, currency.getDecimalPlaces());
        return new StandardMoney(Money.of(currency, bd));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an instance of {@code StandardMoney} from the provider, rounding as necessary.
     * <p>
     * This allows you to create an instance from any class that implements the
     * provider, such as {@code Money}.
     * No rounding is performed on the amount, so it must have a scale compatible
     * with the currency.
     *
     * @param moneyProvider  the money to convert, not null
     * @return the new instance, never null
     * @throws ArithmeticException if the scale exceeds the currency scale
     */
    public static StandardMoney from(MoneyProvider moneyProvider) {
        return StandardMoney.from(moneyProvider, RoundingMode.UNNECESSARY);
    }

    /**
     * Gets an instance of {@code StandardMoney} from the provider, rounding as necessary.
     * <p>
     * This allows you to create an instance from any class that implements the
     * provider, such as {@code Money}.
     * The rounding mode is used to adjust the scale to the scale of the currency.
     *
     * @param moneyProvider  the money to convert, not null
     * @param roundingMode  the rounding mode to use, not null
     * @return the new instance, never null
     * @throws ArithmeticException if the rounding fails
     */
    public static StandardMoney from(MoneyProvider moneyProvider, RoundingMode roundingMode) {
        MoneyUtils.checkNotNull(moneyProvider, "MoneyProvider must not be null");
        MoneyUtils.checkNotNull(roundingMode, "RoundingMode must not be null");
        return new StandardMoney(Money.from(moneyProvider).withCurrencyScale(roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Parses an instance of {@code StandardMoney} from a string.
     * <p>
     * The string format is '<currencyCode> <amount>'.
     * The currency code must be three letters, and the amount must be a number.
     * This matches the output from {@link #toString()}.
     * <p>
     * For example, {@code of("USD 25")} creates the instance {@code USD 25.00}
     * while {@code of("USD 25.95")} creates the instance {@code USD 25.95}.
     *
     * @param moneyStr  the money string to parse, not null
     * @return the parsed instance, never null
     * @throws IllegalArgumentException if the string is malformed
     * @throws ArithmeticException if the amount is too large
     */
    public static StandardMoney parse(String moneyStr) {
        MoneyUtils.checkNotNull(moneyStr, "Money must not be null");
        if (moneyStr.length() < 5 || moneyStr.charAt(3) != ' ') {
            throw new IllegalArgumentException("Money '" + moneyStr + "' cannot be parsed");
        }
        String currStr = moneyStr.substring(0, 3);
        String amountStr = moneyStr.substring(4);
        return StandardMoney.of(CurrencyUnit.of(currStr), new BigDecimal(amountStr));
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor, creating a new monetary instance.
     * 
     * @param money  the underlying money, not null
     */
    private StandardMoney(Money money) {
        assert money != null : "Joda-Money bug: Money must not be null";
        assert money.isCurrencyScale() : "Joda-Money bug: Only currency scale is valid for StandardMoney";
        iMoney = money;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new {@code StandardMoney}, returning {@code this} if possible.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param currency  the currency to use, not null
     * @return the new instance with the input currency set, never null
     */
    private StandardMoney with(Money newInstance) {
        if (newInstance == iMoney) {
            return this;
        }
        return new StandardMoney(newInstance);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the currency.
     * 
     * @return the currency, never null
     */
    public CurrencyUnit getCurrencyUnit() {
        return iMoney.getCurrencyUnit();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value with the specified currency.
     * <p>
     * The returned instance will have the specified currency and the amount
     * from this instance. If the scale differs between the currencies such
     * that rounding would be required, then an exception is thrown.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param currency  the currency to use, not null
     * @return the new instance with the input currency set, never null
     * @throws ArithmeticException if the scale of the new currency is less than
     *  the scale of this currency
     */
    public StandardMoney withCurrencyUnit(CurrencyUnit currency) {
        return withCurrencyUnit(currency, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the specified currency.
     * <p>
     * The returned instance will have the specified currency and the amount
     * from this instance. If the number of decimal places differs between the
     * currencies, then the amount may be rounded.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param currency  the currency to use, not null
     * @param roundingMode  the rounding mode to use to bring the decimal places back in line, not null
     * @return the new instance with the input currency set, never null
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney withCurrencyUnit(CurrencyUnit currency, RoundingMode roundingMode) {
        return with(iMoney.withCurrencyUnit(currency).withCurrencyScale(roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the scale of the {@code BigDecimal} amount.
     * <p>
     * The scale has the same meaning as in {@link BigDecimal}.
     * Positive values represent the number of decimal places in use.
     * Negative numbers represent the opposite.
     * For example, a scale of 2 means that the money will have two decimal places
     * such as 'USD 43.25'.
     * <p>
     * For {@code StandardMoney}, the scale is fixed and always matches that of the currency.
     * 
     * @return the scale in use, typically 2 but could be 0, 1 and 3
     */
    public int getScale() {
        return iMoney.getScale();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the amount.
     * <p>
     * This returns the value of the money as a {@code BigDecimal}.
     * The scale will be the scale of this money.
     * 
     * @return the amount, never null
     */
    public BigDecimal getAmount() {
        return iMoney.getAmount();
    }

    /**
     * Gets the amount in major units as a {@code BigDecimal} with scale 0.
     * <p>
     * This returns the monetary amount in terms of the major units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 2, and 'BHD -1.345' will return -1.
     * <p>
     * This is returned as a {@code BigDecimal} rather than a {@code BigInteger}.
     * This is to allow further calculations to be performed on the result.
     * Should you need a {@code BigInteger}, simply call {@link BigDecimal#toBigInteger()}.
     * 
     * @return the major units part of the amount, never null
     */
    public BigDecimal getAmountMajor() {
        return iMoney.getAmountMajor();
    }

    /**
     * Gets the amount in major units as a {@code long}.
     * <p>
     * This returns the monetary amount in terms of the major units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 2, and 'BHD -1.345' will return -1.
     * 
     * @return the major units part of the amount
     * @throws ArithmeticException if the amount is too large for a {@code long}
     */
    public long getAmountMajorLong() {
        return iMoney.getAmountMajorLong();
    }

    /**
     * Gets the amount in major units as an {@code int}.
     * <p>
     * This returns the monetary amount in terms of the major units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 2, and 'BHD -1.345' will return -1.
     * 
     * @return the major units part of the amount
     * @throws ArithmeticException if the amount is too large for an {@code int}
     */
    public int getAmountMajorInt() {
        return iMoney.getAmountMajorInt();
    }

    /**
     * Gets the amount in minor units as a {@code BigDecimal} with scale 0.
     * <p>
     * This returns the monetary amount in terms of the minor units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 235, and 'BHD -1.345' will return -1345.
     * <p>
     * This is returned as a {@code BigDecimal} rather than a {@code BigInteger}.
     * This is to allow further calculations to be performed on the result.
     * Should you need a {@code BigInteger}, simply call {@link BigDecimal#toBigInteger()}.
     * 
     * @return the minor units part of the amount, never null
     */
    public BigDecimal getAmountMinor() {
        return iMoney.getAmountMinor();
    }

    /**
     * Gets the amount in minor units as a {@code long}.
     * <p>
     * This returns the monetary amount in terms of the minor units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 235, and 'BHD -1.345' will return -1345.
     * 
     * @return the minor units part of the amount
     * @throws ArithmeticException if the amount is too large for a {@code long}
     */
    public long getAmountMinorLong() {
        return iMoney.getAmountMinorLong();
    }

    /**
     * Gets the amount in minor units as an {@code int}.
     * <p>
     * This returns the monetary amount in terms of the minor units of the currency,
     * truncating the amount if necessary.
     * For example, 'EUR 2.35' will return 235, and 'BHD -1.345' will return -1345.
     * 
     * @return the minor units part of the amount
     * @throws ArithmeticException if the amount is too large for an {@code int}
     */
    public int getAmountMinorInt() {
        return iMoney.getAmountMinorInt();
    }

    /**
     * Gets the minor part of the amount.
     * <p>
     * This return the minor unit part of the monetary amount.
     * This is defined as the amount in minor units excluding major units.
     * <p>
     * For example, EUR has a scale of 2, so the minor part is always between 0 and 99
     * for positive amounts, and 0 and -99 for negative amounts.
     * Thus 'EUR 2.35' will return 35, and 'EUR -1.34' will return -34.
     * 
     * @return the minor part of the amount, negative if the amount is negative
     */
    public int getMinorPart() {
        return iMoney.getMinorPart();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the amount is zero.
     * 
     * @return true if the amount is zero
     */
    public boolean isZero() {
        return iMoney.isZero();
    }

    /**
     * Checks if the amount is greater than zero.
     * 
     * @return true if the amount is greater than zero
     */
    public boolean isPositive() {
        return iMoney.isPositive();
    }

    /**
     * Checks if the amount is zero or greater.
     * 
     * @return true if the amount is zero or greater
     */
    public boolean isPositiveOrZero() {
        return iMoney.isPositiveOrZero();
    }

    /**
     * Checks if the amount is less than zero.
     * 
     * @return true if the amount is less than zero
     */
    public boolean isNegative() {
        return iMoney.isNegative();
    }

    /**
     * Checks if the amount is zero or less.
     * 
     * @return true if the amount is zero or less
     */
    public boolean isNegativeOrZero() {
        return iMoney.isNegativeOrZero();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value with the specified amount.
     * <p>
     * The returned instance will have this currency and the new amount.
     * No rounding is performed on the amount to be added, so it must have a
     * scale compatible with the currency.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amount  the monetary amount to set in the returned instance, not null
     * @return the new instance with the input amount set, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney withAmount(BigDecimal amount) {
        return withAmount(amount, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the specified amount.
     * <p>
     * The returned instance will have this currency and the new amount.
     * If the scale of the {@code BigDecimal} needs to be adjusted, then
     * it will be rounded using the specified mode.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amount  the monetary amount to set in the returned instance, not null
     * @param roundingMode  the rounding mode to adjust the scale, not null
     * @return the new instance with the input amount set, never null
     */
    public StandardMoney withAmount(BigDecimal amount, RoundingMode roundingMode) {
        return with(iMoney.withAmount(amount).withCurrencyScale(roundingMode));
    }

    /**
     * Returns a copy of this monetary value with the specified amount using a well-defined
     * conversion from a {@code double}.
     * <p>
     * The returned instance will have this currency and the new amount.
     * No rounding is performed on the amount to be added, so it must have a
     * scale compatible with the currency.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amount  the monetary amount to set in the returned instance, not null
     * @return the new instance with the input amount set, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney withAmount(double amount) {
        return withAmount(amount, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the specified amount using a well-defined
     * conversion from a {@code double}.
     * <p>
     * The returned instance will have this currency and the new amount.
     * If the scale of the {@code BigDecimal} needs to be adjusted, then
     * it will be rounded using the specified mode.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amount  the monetary amount to set in the returned instance, not null
     * @param roundingMode  the rounding mode to adjust the scale, not null
     * @return the new instance with the input amount set, never null
     */
    public StandardMoney withAmount(double amount, RoundingMode roundingMode) {
        return with(iMoney.withAmount(amount).withCurrencyScale(roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value with the amount added.
     * <p>
     * The result is the simple addition of the two values and is always accurate.
     * For example,'USD 25.95' plus 'USD 3.02' will 'USD 28.97'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param moneyToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     * @throws MoneyException if the currencies differ
     */
    public StandardMoney plus(StandardMoney moneyToAdd) {
        return with(iMoney.plus(moneyToAdd));
    }

    /**
     * Returns a copy of this monetary value with the amount added.
     * <p>
     * The result is the addition of the two values.
     * No rounding is performed on the amount to be added, so it must have a
     * scale compatible with the currency.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney plus(BigDecimal amountToAdd) {
        return plus(amountToAdd, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the amount added.
     * <p>
     * The result is the addition of the two values.
     * If the amount to add exceeds the scale of the currency, then the
     * rounding mode will be used to adjust the result.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     */
    public StandardMoney plus(BigDecimal amountToAdd, RoundingMode roundingMode) {
        return with(iMoney.plus(amountToAdd).withCurrencyScale(roundingMode));
    }

    /**
     * Returns a copy of this monetary value with the amount added.
     * <p>
     * The result is the addition of the two values.
     * No rounding is performed on the amount to be added, so it must have a
     * scale compatible with the currency.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney plus(double amountToAdd) {
        return plus(amountToAdd, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the amount added.
     * <p>
     * The result is the addition of the two values.
     * If the amount to add exceeds the scale of the currency, then the
     * rounding mode will be used to adjust the result.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     */
    public StandardMoney plus(double amountToAdd, RoundingMode roundingMode) {
        return with(iMoney.plus(amountToAdd).withCurrencyScale(roundingMode));
    }

    /**
     * Returns a copy of this monetary value with the amount in major units added.
     * <p>
     * This adds an amount in major units, leaving the minor units untouched.
     * For example, USD 23.45 plus 138 gives USD 161.45.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     */
    public StandardMoney plusMajor(long amountToAdd) {
        return with(iMoney.plusMajor(amountToAdd));
    }

    /**
     * Returns a copy of this monetary value with the amount in minor units added.
     * <p>
     * This adds an amount in minor units.
     * For example, USD 23.45 plus 138 gives USD 24.83.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToAdd  the monetary value to add, not null
     * @return the new instance with the input amount added, never null
     */
    public StandardMoney plusMinor(long amountToAdd) {
        return with(iMoney.plusMinor(amountToAdd));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value with the amount subtracted.
     * <p>
     * The result is the simple subtraction of the two values and is always accurate.
     * For example,'USD 25.95' minus 'USD 3.02' will 'USD 22.93'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param moneyToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     * @throws MoneyException if the currencies differ
     */
    public StandardMoney minus(StandardMoney moneyToSubtract) {
        return with(iMoney.minus(moneyToSubtract));
    }

    /**
     * Returns a copy of this monetary value with the amount subtracted.
     * <p>
     * The result is the subtraction of the two values.
     * No rounding is performed on the amount to be subtracted, so it must have a
     * scale compatible with the currency.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney minus(BigDecimal amountToSubtract) {
        return minus(amountToSubtract, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the amount subtracted.
     * <p>
     * The result is the subtraction of the two values.
     * If the amount to subtract exceeds the scale of the currency, then the
     * rounding mode will be used to adjust the result.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     */
    public StandardMoney minus(BigDecimal amountToSubtract, RoundingMode roundingMode) {
        return with(iMoney.minus(amountToSubtract).withCurrencyScale(roundingMode));
    }

    /**
     * Returns a copy of this monetary value with the amount subtracted.
     * <p>
     * The result is the subtraction of the two values.
     * No rounding is performed on the amount to be subtracted, so it must have a
     * scale compatible with the currency.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     * @throws ArithmeticException if the scale of the amount is too large
     */
    public StandardMoney minus(double amountToSubtract) {
        return minus(amountToSubtract, RoundingMode.UNNECESSARY);
    }

    /**
     * Returns a copy of this monetary value with the amount subtracted.
     * <p>
     * The result is the subtraction of the two values.
     * If the amount to subtract exceeds the scale of the currency, then the
     * rounding mode will be used to adjust the result.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     */
    public StandardMoney minus(double amountToSubtract, RoundingMode roundingMode) {
        return with(iMoney.minus(amountToSubtract).withCurrencyScale(roundingMode));
    }

    /**
     * Returns a copy of this monetary value with the amount in major units subtracted.
     * <p>
     * This subtracts an amount in major units, leaving the minor units untouched.
     * For example, USD 23.45 minus 138 gives USD -114.55.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     */
    public StandardMoney minusMajor(long amountToSubtract) {
        return with(iMoney.minusMajor(amountToSubtract));
    }

    /**
     * Returns a copy of this monetary value with the amount in minor units subtracted.
     * <p>
     * This subtracts an amount in minor units.
     * For example, USD 23.45 minus 138 gives USD 22.07.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param amountToSubtract  the monetary value to subtract, not null
     * @return the new instance with the input amount subtracted, never null
     */
    public StandardMoney minusMinor(long amountToSubtract) {
        return with(iMoney.minusMinor(amountToSubtract));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value multiplied by the specified value.
     * <p>
     * This takes this amount and multiplies it by the specified value, rounding
     * the result is rounded as specified.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToMultiplyBy  the scalar value to multiply by, not null
     * @param roundingMode  the rounding mode to use to bring the decimal places back in line, not null
     * @return the new multiplied instance, never null
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney multipliedBy(BigDecimal valueToMultiplyBy, RoundingMode roundingMode) {
        return with(iMoney.multiplyRetainScale(valueToMultiplyBy, roundingMode));
    }

    /**
     * Returns a copy of this monetary value multiplied by the specified value.
     * <p>
     * This takes this amount and multiplies it by the specified value, rounding
     * the result is rounded as specified.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToMultiplyBy  the scalar value to multiply by, not null
     * @param roundingMode  the rounding mode to use to bring the decimal places back in line, not null
     * @return the new multiplied instance, never null
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney multipliedBy(double valueToMultiplyBy, RoundingMode roundingMode) {
        return with(iMoney.multiplyRetainScale(valueToMultiplyBy, roundingMode));
    }

    /**
     * Returns a copy of this monetary value multiplied by the specified value.
     * <p>
     * This takes this amount and multiplies it by the specified value.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToMultiplyBy  the scalar value to multiply by, not null
     * @return the new multiplied instance, never null
     */
    public StandardMoney multipliedBy(long valueToMultiplyBy) {
        return with(iMoney.multipliedBy(valueToMultiplyBy));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value divided by the specified value.
     * <p>
     * This takes this amount and divides it by the specified value, rounding
     * the result is rounded as specified.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToDivideBy  the scalar value to divide by, not null
     * @param roundingMode  the rounding mode to use, not null
     * @return the new divided instance, never null
     * @throws ArithmeticException if dividing by zero
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney dividedBy(BigDecimal valueToDivideBy, RoundingMode roundingMode) {
        return with(iMoney.dividedBy(valueToDivideBy, roundingMode));
    }

    /**
     * Returns a copy of this monetary value divided by the specified value.
     * <p>
     * This takes this amount and divides it by the specified value, rounding
     * the result is rounded as specified.
     * <p>
     * The amount is converted via {@link BigDecimal#valueOf(double)} which yields
     * the most expected answer for most programming scenarios.
     * Any {@code double} literal in code will be converted to
     * exactly the same BigDecimal with the same scale.
     * For example, the literal '1.45d' will be converted to '1.45'.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToDivideBy  the scalar value to divide by, not null
     * @param roundingMode  the rounding mode to use, not null
     * @return the new divided instance, never null
     * @throws ArithmeticException if dividing by zero
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney dividedBy(double valueToDivideBy, RoundingMode roundingMode) {
        return with(iMoney.dividedBy(valueToDivideBy, roundingMode));
    }

    /**
     * Returns a copy of this monetary value divided by the specified value.
     * <p>
     * This takes this amount and divides it by the specified value, rounding
     * the result is rounded as specified.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param valueToDivideBy  the scalar value to divide by, not null
     * @return the new divided instance, never null
     * @throws ArithmeticException if dividing by zero
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney dividedBy(long valueToDivideBy, RoundingMode roundingMode) {
        return with(iMoney.dividedBy(valueToDivideBy, roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value with the amount negated.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @return the new instance with the amount negated, never null
     */
    public StandardMoney negated() {
        return with(iMoney.negated());
    }

    /**
     * Returns a copy of this monetary value with a positive amount.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @return the new instance with the amount converted to be positive, never null
     */
    public StandardMoney abs() {
        return (isNegative() ? negated() : this);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value rounded to the specified scale without
     * changing the current scale.
     * <p>
     * Scale is described in {@link BigDecimal} and represents the point below which
     * the monetary value is zero. Negative scales round increasingly large numbers.
     * <ul>
     * <li>Rounding 'EUR 45.23' to a scale of -1 returns 40.00 or 50.00 depending on the rounding mode.
     * <li>Rounding 'EUR 45.23' to a scale of 0 returns 45.00 or 46.00 depending on the rounding mode.
     * <li>Rounding 'EUR 45.23' to a scale of 1 returns 45.20 or 45.30 depending on the rounding mode.
     * <li>Rounding 'EUR 45.23' to a scale of 2 has no effect (it already has that scale).
     * <li>Rounding 'EUR 45.23' to a scale of 3 has no effect (the scale is not increased).
     * </ul>
     * This instance is immutable and unaffected by this method.
     * 
     * @param scale  the new scale
     * @param roundingMode  the rounding mode to use, not null
     * @return the new instance with the amount converted to be positive, never null
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney rounded(int scale, RoundingMode roundingMode) {
        return with(iMoney.rounded(scale, roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this monetary value converted into another currency
     * using the specified conversion rate, with a rounding mode used to adjust
     * the decimal places in the result.
     * <p>
     * This instance is immutable and unaffected by this method.
     * 
     * @param currency  the new currency, not null
     * @param conversionMultipler  the conversion factor between the currencies, not null
     * @param roundingMode  the rounding mode to use to bring the decimal places back in line, not null
     * @return the new multiplied instance, never null
     * @throws MoneyException if the currency is the same as this currency
     * @throws MoneyException if the conversion multiplier is negative
     * @throws ArithmeticException if the rounding fails
     */
    public StandardMoney convertedTo(CurrencyUnit currency, BigDecimal conversionMultipler, RoundingMode roundingMode) {
        return with(iMoney.convertedTo(currency, conversionMultipler).withCurrencyScale(roundingMode));
    }

    //-----------------------------------------------------------------------
    /**
     * Implements the {@code MoneyProvider} interface, returning a
     * {@code Money} instance with the same currency, amount and scale.
     * 
     * @return the money instance, never null
     * @throws MoneyException if conversion is not possible
     */
    public Money toMoney() {
        return iMoney;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this instance and the specified instance have the same currency.
     * 
     * @param money  the money to check, not null
     * @return true if they have the same currency
     */
    public boolean isSameCurrency(MoneyProvider money) {
        return iMoney.isSameCurrency(money);
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this monetary value to another.
     * The compared values must be in the same currency.
     * 
     * @param other  the other monetary value, not null
     * @return -1 if this is less than , 0 if equal, 1 if greater than
     * @throws MoneyException if the currencies differ
     */
    public int compareTo(MoneyProvider other) {
        return iMoney.compareTo(other);
    }

    /**
     * Checks if this monetary value is equal to another.
     * <p>
     * This allows {@code StandardMoney} to be compared to {@code Money}.
     * Scale is ignored, so 'USD 30.00' and 'USD 30' are equal.
     * <p>
     * The compared values must be in the same currency.
     * 
     * @param other  the other monetary value, not null
     * @return true is this is greater than the specified monetary value
     * @throws MoneyException if the currencies differ
     * @see #equals(Object)
     */
    public boolean isEqual(MoneyProvider other) {
        return iMoney.isEqual(other);
    }

    /**
     * Checks if this monetary value is greater than another.
     * The compared values must be in the same currency.
     * 
     * @param other  the other monetary value, not null
     * @return true is this is greater than the specified monetary value
     * @throws MoneyException if the currencies differ
     */
    public boolean isGreaterThan(MoneyProvider other) {
        return iMoney.isGreaterThan(other);
    }

    /**
     * Checks if this monetary value is less than another.
     * The compared values must be in the same currency.
     * 
     * @param other  the other monetary value, not null
     * @return true is this is less than the specified monetary value
     * @throws MoneyException if the currencies differ
     */
    public boolean isLessThan(MoneyProvider other) {
        return iMoney.isLessThan(other);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this monetary value equals another.
     * The compared values must be in the same currency.
     * 
     * @return true if this instance equals the other instance
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof StandardMoney) {
            StandardMoney otherMoney = (StandardMoney) other;
            return iMoney.equals(otherMoney.iMoney);
        }
        return false;
    }

    /**
     * Returns a hash code for this monetary value.
     * 
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return iMoney.hashCode() + 3;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the monetary value as a string.
     * <p>
     * The format is the 3 letter ISO currency code, followed by a space,
     * followed by the amount as per {@link BigDecimal#toPlainString()}.
     * 
     * @return the string representation of this monetary value, never null
     */
    @Override
    public String toString() {
        return iMoney.toString();
    }

}