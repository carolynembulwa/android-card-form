package com.braintreepayments.cardform.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.braintreepayments.cardform.CardScanningFragment;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.OnCardFormValidListener;
import com.braintreepayments.cardform.R;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.utils.ViewUtils;
import com.braintreepayments.cardform.view.CardEditText.OnCardTypeChangedListener;

import java.util.ArrayList;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

public class CardForm extends LinearLayout implements OnCardTypeChangedListener, OnFocusChangeListener, OnClickListener,
        OnEditorActionListener, TextWatcher {

    private List<ErrorEditText> mVisibleEditTexts;

    private ImageView mCardNumberIcon;
    private CardEditText mCardNumber;
    private ExpirationDateEditText mExpiration;
    private CvvEditText mCvv;
    private ImageView mPostalCodeIcon;
    private PostalCodeEditText mPostalCode;
    private ImageView mMobileNumberIcon;
    private CountryCodeEditText mCountryCode;
    private MobileNumberEditText mMobileNumber;
    private TextView mMobileNumberExplanation;

    private boolean mCardNumberRequired;
    private boolean mExpirationRequired;
    private boolean mCvvRequired;
    private boolean mPostalCodeRequired;
    private boolean mMobileNumberRequired;
    private String mActionLabel;

    private boolean mValid = false;

    private OnCardFormValidListener mOnCardFormValidListener;
    private OnCardFormSubmitListener mOnCardFormSubmitListener;
    private OnCardFormFieldFocusedListener mOnCardFormFieldFocusedListener;
    private OnCardTypeChangedListener mOnCardTypeChangedListener;

    public CardForm(Context context) {
        super(context);
        init();
    }

    public CardForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public CardForm(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(GONE);
        setOrientation(VERTICAL);

        inflate(getContext(), R.layout.bt_card_form_fields, this);

        mCardNumberIcon = (ImageView) findViewById(R.id.bt_card_form_card_number_icon);
        mCardNumber = (CardEditText) findViewById(R.id.bt_card_form_card_number);
        mExpiration = (ExpirationDateEditText) findViewById(R.id.bt_card_form_expiration);
        mCvv = (CvvEditText) findViewById(R.id.bt_card_form_cvv);
        mPostalCodeIcon = (ImageView) findViewById(R.id.bt_card_form_postal_code_icon);
        mPostalCode = (PostalCodeEditText) findViewById(R.id.bt_card_form_postal_code);
        mMobileNumberIcon = (ImageView) findViewById(R.id.bt_card_form_mobile_number_icon);
        mCountryCode = (CountryCodeEditText) findViewById(R.id.bt_card_form_country_code);
        mMobileNumber = (MobileNumberEditText) findViewById(R.id.bt_card_form_mobile_number);
        mMobileNumberExplanation = (TextView) findViewById(R.id.bt_card_form_mobile_number_explanation);

        mVisibleEditTexts = new ArrayList<>();

        setListeners(mCardNumber);
        setListeners(mExpiration);
        setListeners(mCvv);
        setListeners(mPostalCode);
        setListeners(mMobileNumber);

        mCardNumber.setOnCardTypeChangedListener(this);
    }

    /**
     * @param required {@code true} to show and require a credit card number, {@code false} otherwise. Defaults to {@code false}.
     * @return {@link CardForm} for method chaining
     */
    public CardForm cardRequired(boolean required) {
        mCardNumberRequired = required;
        return this;
    }

    /**
     * @param required {@code true} to show and require an expiration date, {@code false} otherwise. Defaults to {@code false}.
     * @return {@link CardForm} for method chaining
     */
    public CardForm expirationRequired(boolean required) {
        mExpirationRequired = required;
        return this;
    }

    /**
     * @param required {@code true} to show and require a cvv, {@code false} otherwise. Defaults to {@code false}.
     * @return {@link CardForm} for method chaining
     */
    public CardForm cvvRequired(boolean required) {
        mCvvRequired = required;
        return this;
    }

    /**
     * @param required {@code true} to show and require a postal code, {@code false} otherwise. Defaults to {@code false}.
     * @return {@link CardForm} for method chaining
     */
    public CardForm postalCodeRequired(boolean required) {
        mPostalCodeRequired = required;
        return this;
    }

    /**
     * @param required {@code true} to show and require a mobile number, {@code false} otherwise. Defaults to {@code false}.
     * @return {@link CardForm} for method chaining
     */
    public CardForm mobileNumberRequired(boolean required) {
        mMobileNumberRequired = required;
        return this;
    }

    /**
     * @param actionLabel the {@link java.lang.String} to display to the user to submit the form from the keyboard
     * @return {@link CardForm} for method chaining
     */
    public CardForm actionLabel(String actionLabel) {
        mActionLabel = actionLabel;
        return this;
    }

    /**
     * @param mobileNumberExplanation the {@link java.lang.String} to display below the mobile number input
     * @return {@link CardForm} for method chaining
     */
    public CardForm mobileNumberExplanation(String mobileNumberExplanation) {
        mMobileNumberExplanation.setText(mobileNumberExplanation);
        return this;
    }

    /**
     * Sets up the card form for display to the user using the values provided in {@link CardForm#cardRequired(boolean)},
     * {@link CardForm#expirationRequired(boolean)}, ect. If {@link #setup(android.app.Activity)} is not called,
     * the form will not be visible.
     *
     * @param activity Used to set {@link android.view.WindowManager.LayoutParams#FLAG_SECURE} to prevent screenshots
     */
    public void setup(Activity activity) {
        if (SDK_INT >= ICE_CREAM_SANDWICH) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }

        boolean isDarkBackground = ViewUtils.isDarkBackground(activity);
        mCardNumberIcon.setImageResource(isDarkBackground ? R.drawable.bt_ic_card_dark : R.drawable.bt_ic_card);
        mPostalCodeIcon.setImageResource(isDarkBackground ? R.drawable.bt_ic_postal_code_dark : R.drawable.bt_ic_postal_code);
        mMobileNumberIcon.setImageResource(isDarkBackground? R.drawable.bt_ic_mobile_number_dark : R.drawable.bt_ic_mobile_number);

        mExpiration.setActivity(activity);

        setViewVisibility(mCardNumberIcon, mCardNumberRequired);
        setFieldVisibility(mCardNumber, mCardNumberRequired);
        setFieldVisibility(mExpiration, mExpirationRequired);
        setFieldVisibility(mCvv, mCvvRequired);
        setViewVisibility(mPostalCodeIcon, mPostalCodeRequired);
        setFieldVisibility(mPostalCode, mPostalCodeRequired);
        setViewVisibility(mMobileNumberIcon, mMobileNumberRequired);
        setFieldVisibility(mCountryCode, mMobileNumberRequired);
        setFieldVisibility(mMobileNumber, mMobileNumberRequired);
        setViewVisibility(mMobileNumberExplanation, mMobileNumberRequired);

        TextInputEditText editText;
        for (int i = 0; i < mVisibleEditTexts.size(); i++) {
            editText = mVisibleEditTexts.get(i);
            if (i == mVisibleEditTexts.size() - 1) {
                editText.setNextFocusDownId(NO_ID);
                editText.setImeOptions(EditorInfo.IME_ACTION_GO);
                editText.setImeActionLabel(mActionLabel, EditorInfo.IME_ACTION_GO);
                editText.setOnEditorActionListener(this);
            } else {
                editText.setNextFocusDownId(mVisibleEditTexts.get(i + 1).getId());
                editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                editText.setImeActionLabel(null, EditorInfo.IME_ACTION_NONE);
                editText.setOnEditorActionListener(null);
            }
        }

        setVisibility(VISIBLE);
    }

    /**
     * Check if card scanning is available.
     *
     * Card scanning requires the card.io dependency and camera support.
     *
     * @return {@code true} if available, {@code false} otherwise.
     */
    public boolean isCardScanningAvailable() {
        try {
            return CardIOActivity.canReadCardWithCamera();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    /**
     * Launches card.io card scanning is {@link #isCardScanningAvailable()} is {@code true}.
     *
     * @param activity
     */
    public void scanCard(Activity activity) {
        if (isCardScanningAvailable()) {
            CardScanningFragment.requestScan(activity, this);
        }
    }

    public void handleCardIOResponse(Intent data) {
        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            if (mCardNumberRequired) {
                mCardNumber.setText(scanResult.cardNumber);
                mCardNumber.focusNextView();
            }

            if (scanResult.isExpiryValid() && mExpirationRequired) {
                mExpiration.setText(Integer.toString(scanResult.expiryMonth) + Integer.toString(scanResult.expiryYear));
                mExpiration.focusNextView();
            }
        }
    }

    private void setListeners(EditText editText) {
        editText.setOnFocusChangeListener(this);
        editText.setOnClickListener(this);
        editText.addTextChangedListener(this);
    }

    private void setViewVisibility(View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }

    private void setFieldVisibility(ErrorEditText editText, boolean visible) {
        editText.setVisibility(visible ? VISIBLE : GONE);
        if (editText.getTextInputLayoutParent() != null) {
            editText.getTextInputLayoutParent().setVisibility(visible ? VISIBLE : GONE);
        }

        if (visible) {
            mVisibleEditTexts.add(editText);
        } else {
            mVisibleEditTexts.remove(editText);
        }
    }

    /**
     * Use to enable or disable entry of the expiration date using a dialog. Defaults to using the dialog.
     *
     * @param activity used as the parent activity for the dialog
     * @param useDialog {@code false} to use a numeric keyboard to enter the expiration date, {@code true} to use a
     *        custom dialog to enter the expiration date. Defaults to {@code true}.
     */
    public void useDialogForExpirationDateEntry(Activity activity, boolean useDialog) {
        mExpiration.useDialogForExpirationDateEntry(activity, useDialog);
    }

    /**
     * Enable or disable showing card type icons as part of the {@link CardEditText}. Defaults to {@code true}.
     *
     * @param display {@code true} to display card type icons, {@code false} to never display card type icons.
     */
    public void displayCardTypeIcons(boolean display) {
        mCardNumber.setDisplayCardTypeIcon(display);
    }

    /**
     * Set the listener to receive a callback when the card form becomes valid or invalid
     * @param listener to receive the callback
     */
    public void setOnCardFormValidListener(OnCardFormValidListener listener) {
        mOnCardFormValidListener = listener;
    }

    /**
     * Set the listener to receive a callback when the card form should be submitted.
     * Triggered from a keyboard by a {@link android.view.inputmethod.EditorInfo#IME_ACTION_GO} event
     *
     * @param listener to receive the callback
     */
    public void setOnCardFormSubmitListener(OnCardFormSubmitListener listener) {
        mOnCardFormSubmitListener = listener;
    }

    /**
     * Set the listener to receive a callback when a field is focused
     *
     * @param listener to receive the callback
     */
    public void setOnFormFieldFocusedListener(OnCardFormFieldFocusedListener listener) {
        mOnCardFormFieldFocusedListener = listener;
    }

    /**
     * Set the listener to receive a callback when the {@link com.braintreepayments.cardform.utils.CardType} changes.
     *
     * @param listener to receive the callback
     */
    public void setOnCardTypeChangedListener(OnCardTypeChangedListener listener) {
        mOnCardTypeChangedListener = listener;
    }

    /**
     * Set {@link android.widget.EditText} fields as enabled or disabled
     *
     * @param enabled {@code true} to enable all required fields, {@code false} to disable all required fields
     */
    public void setEnabled(boolean enabled) {
        mCardNumber.setEnabled(enabled);
        mExpiration.setEnabled(enabled);
        mCvv.setEnabled(enabled);
        mPostalCode.setEnabled(enabled);
        mMobileNumber.setEnabled(enabled);
    }

    /**
     * @return {@code true} if all require fields are valid, otherwise {@code false}
     */
    public boolean isValid() {
        boolean valid = true;
        if (mCardNumberRequired) {
            valid = valid && mCardNumber.isValid();
        }
        if (mExpirationRequired) {
            valid = valid && mExpiration.isValid();
        }
        if (mCvvRequired) {
            valid = valid && mCvv.isValid();
        }
        if (mPostalCodeRequired) {
            valid = valid && mPostalCode.isValid();
        }
        if (mMobileNumberRequired) {
            valid = valid && mCountryCode.isValid() && mMobileNumber.isValid();
        }
        return valid;
    }

    /**
     * Validate all required fields and mark invalid fields with an error indicator
     */
    public void validate() {
        if (mCardNumberRequired) {
            mCardNumber.validate();
        }
        if (mExpirationRequired) {
            mExpiration.validate();
        }
        if (mCvvRequired) {
            mCvv.validate();
        }
        if (mPostalCodeRequired) {
            mPostalCode.validate();
        }
        if (mMobileNumberRequired) {
            mCountryCode.validate();
            mMobileNumber.validate();
        }
    }

    /**
     * @return {@link CardEditText} view in the card form
     */
    public CardEditText getCardEditText() {
        return mCardNumber;
    }

    /**
     * @return {@link ExpirationDateEditText} view in the card form
     */
    public ExpirationDateEditText getExpirationDateEditText() {
        return mExpiration;
    }

    /**
     * @return {@link CvvEditText} view in the card form
     */
    public CvvEditText getCvvEditText() {
        return mCvv;
    }

    /**
     * @return {@link PostalCodeEditText} view in the card form
     */
    public PostalCodeEditText getPostalCodeEditText() {
        return mPostalCode;
    }

    /**
     * @return {@link CountryCodeEditText} view in the card form
     */
    public CountryCodeEditText getCountryCodeEditText() {
        return mCountryCode;
    }

    /**
     * @return {@link MobileNumberEditText} view in the card form
     */
    public MobileNumberEditText getMobileNumberEditText() {
        return mMobileNumber;
    }

    /**
     * Set visual indicator on card number to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setCardNumberError(String errorMessage) {
        if (mCardNumberRequired) {
            mCardNumber.setError(errorMessage);
            requestEditTextFocus(mCardNumber);
        }
    }

    /**
     * Set visual indicator on expiration to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setExpirationError(String errorMessage) {
        if (mExpirationRequired) {
            mExpiration.setError(errorMessage);
            if (!mCardNumber.isFocused()) {
                requestEditTextFocus(mExpiration);
            }
        }
    }

    /**
     * Set visual indicator on cvv to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setCvvError(String errorMessage) {
        if (mCvvRequired) {
            mCvv.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused()) {
                requestEditTextFocus(mCvv);
            }
        }
    }

    /**
     * Set visual indicator on postal code to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setPostalCodeError(String errorMessage) {
        if (mPostalCodeRequired) {
            mPostalCode.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused() && !mCvv.isFocused()) {
                requestEditTextFocus(mPostalCode);
            }
        }
    }

    /**
     * Set visual indicator on country code to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setCountryCodeError(String errorMessage) {
        if (mMobileNumberRequired) {
            mCountryCode.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused() && !mCvv.isFocused() && !mPostalCode.isFocused()) {
                requestEditTextFocus(mCountryCode);
            }
        }
    }

    /**
     * Set visual indicator on mobile number field to indicate error
     *
     * @param errorMessage the error message to display
     */
    public void setMobileNumberError(String errorMessage) {
        if (mMobileNumberRequired) {
            mMobileNumber.setError(errorMessage);
            if (!mCardNumber.isFocused() && !mExpiration.isFocused() && !mCvv.isFocused() && !mPostalCode.isFocused() && !mCountryCode.isFocused()) {
                requestEditTextFocus(mMobileNumber);
            }
        }
    }

    private void requestEditTextFocus(EditText editText) {
        editText.requestFocus();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Attempt to close the soft keyboard. Will have no effect if the keyboard is not open.
     */
    public void closeSoftKeyboard() {
        mCardNumber.closeSoftKeyboard();
    }

    /**
     * @return the text in the card number field
     */
    public String getCardNumber() {
        return mCardNumber.getText().toString();
    }

    /**
     * @return the 2-digit month, formatted with a leading zero if necessary from the expiration
     * field. If no month has been specified, an empty string is returned.
     */
    public String getExpirationMonth() {
        return mExpiration.getMonth();
    }

    /**
     * @return the 2- or 4-digit year depending on user input from the expiration field.
     * If no year has been specified, an empty string is returned.
     */
    public String getExpirationYear() {
        return mExpiration.getYear();
    }

    /**
     * @return the text in the cvv field
     */
    public String getCvv() {
        return mCvv.getText().toString();
    }

    /**
     * @return the text in the postal code field
     */
    public String getPostalCode() {
        return mPostalCode.getText().toString();
    }

    /**
     * @return the text in the country code field
     */
    public String getCountryCode() {
        return mCountryCode.getCountryCode();
    }

    /**
     * @return the unformatted text in the mobile number field
     */
    public String getMobileNumber() {
        return mMobileNumber.getMobileNumber();
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {
        mCvv.setCardType(cardType);

        if (mOnCardTypeChangedListener != null) {
            mOnCardTypeChangedListener.onCardTypeChanged(cardType);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && mOnCardFormFieldFocusedListener != null) {
            mOnCardFormFieldFocusedListener.onCardFormFieldFocused(v);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnCardFormFieldFocusedListener != null) {
            mOnCardFormFieldFocusedListener.onCardFormFieldFocused(v);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean valid = isValid();
        if (mValid != valid) {
            mValid = valid;
            if (mOnCardFormValidListener != null) {
                mOnCardFormValidListener.onCardFormValid(valid);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO && mOnCardFormSubmitListener != null) {
            mOnCardFormSubmitListener.onCardFormSubmit();
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
