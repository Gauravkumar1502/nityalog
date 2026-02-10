@param String verificationCode
@param String expireInMinutes

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify your Email Address</title>
</head>
<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f7;">
<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f5f5f7; padding: 40px 0;">
    <tr>
        <td align="center">
            <!-- Logo -->
            <table role="presentation" cellpadding="0" cellspacing="0" width="600" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                <tr>
                    <td style="padding: 40px 50px 30px 50px; text-align: center;">
                        <div style="width: 50px; height: 50px; margin: 0 auto 20px; background-color: #f5f5f7; border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="#1d1d1f" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M2 17L12 22L22 17" stroke="#1d1d1f" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M2 12L12 17L22 12" stroke="#1d1d1f" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                    </td>
                </tr>

                <!-- Main Content -->
                <tr>
                    <td style="padding: 0 50px 40px 50px;">
                        <h1 style="font-size: 28px; font-weight: 600; color: #1d1d1f; margin: 0 0 20px 0; text-align: center;">
                            Verify your email address
                        </h1>

                        <p style="font-size: 16px; line-height: 24px; color: #1d1d1f; margin: 0 0 30px 0;">
                            Hello there,
                        </p>

                        <p style="font-size: 16px; line-height: 24px; color: #1d1d1f; margin: 0 0 30px 0;">
                            You're almost done creating your account.
                            Use the verification code below to confirm your email address.
                        </p>

                        <!-- Verification Code -->
                        <div style="background-color: #f5f5f7; border-radius: 8px; padding: 24px; text-align: center; margin: 0 0 30px 0;">
                                <span style="font-size: 32px; font-weight: 600; color: #1d1d1f; letter-spacing: 4px;">
                                    ${verificationCode}
                                </span>
                        </div>

                        <p style="font-size: 14px; line-height: 20px; color: #86868b; margin: 0 0 30px 0;">
                            This verification code expires in ${expireInMinutes} minutes.
                        </p>

                        <div style="border-top: 1px solid #e5e5e7; padding-top: 30px; margin-top: 30px;">
                            <p style="font-size: 16px; line-height: 24px; color: #1d1d1f; margin: 0 0 15px 0; font-weight: 600;">
                                Why you received this email
                            </p>
                            <p style="font-size: 14px; line-height: 20px; color: #86868b; margin: 0 0 20px 0;">
                                We require verification whenever an email address is selected as an account email. Your account cannot be used until you verify it.
                            </p>
                            <p style="font-size: 14px; line-height: 20px; color: #86868b; margin: 0;">
                                If you did not make this request, please disregard this email. No account will be created without verification.
                            </p>
                        </div>

                        <p style="font-size: 16px; line-height: 24px; color: #1d1d1f; margin: 30px 0 0 0;">
                            Kind regards,<br>
                            <strong>Support Team</strong>
                        </p>
                    </td>
                </tr>

                <!-- Footer -->
                <tr>
                    <td style="padding: 30px 50px; background-color: #f5f5f7; border-top: 1px solid #e5e5e7;">
                        <p style="font-size: 12px; line-height: 18px; color: #86868b; margin: 0; text-align: center;">
                            Support | Privacy Policy<br>
                            Copyright © 2025 Your Company. All rights reserved.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>