package com.maidfinder.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maidfinder.app.data.model.UserRole
import com.maidfinder.app.ui.components.AvatarInitials
import com.maidfinder.app.ui.components.GradientButton
import com.maidfinder.app.ui.theme.*

enum class AuthStep { PHONE, OTP, ROLE_SELECT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onDemoLogin: (UserRole) -> Unit,
    onBackClick: () -> Unit
) {
    var currentStep by remember { mutableStateOf(AuthStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CLIENT) }
    var isLoading by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            countdown = 30
            while (countdown > 0) {
                kotlinx.coroutines.delay(1000)
                countdown--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == AuthStep.PHONE) onBackClick()
                        else currentStep = AuthStep.PHONE
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(targetState = currentStep, label = "auth_step") { step ->
                when (step) {
                    AuthStep.PHONE -> PhoneStep(
                        phone = phone,
                        onPhoneChange = { phone = it },
                        isLoading = isLoading,
                        onSendOtp = {
                            isLoading = true
                            otpSent = true
                            currentStep = AuthStep.OTP
                            isLoading = false
                        },
                        focusRequester = focusRequester
                    )
                    AuthStep.OTP -> OtpStep(
                        otp = otp,
                        onOtpChange = { if (it.length <= 6) otp = it },
                        phone = phone,
                        countdown = countdown,
                        onVerify = {
                            isLoading = true
                            currentStep = AuthStep.ROLE_SELECT
                            isLoading = false
                        },
                        onResend = { countdown = 30 },
                        focusRequester = focusRequester
                    )
                    AuthStep.ROLE_SELECT -> RoleSelectStep(
                        selectedRole = selectedRole,
                        onRoleChange = { selectedRole = it },
                        onConfirm = { onLoginSuccess(selectedRole) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Demo mode section
            AnimatedVisibility(
                visible = currentStep == AuthStep.PHONE,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Quick Demo Access",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { onDemoLogin(UserRole.CLIENT) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                        ) {
                            Text("Demo Client", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { onDemoLogin(UserRole.MAID) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                        ) {
                            Text("Demo Maid", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    isLoading: Boolean,
    onSendOtp: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = BluePrimary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Welcome to MaidFinder", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter your phone number to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) onPhoneChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Phone Number") },
            prefix = { Text("+91 ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        GradientButton(
            text = if (isLoading) "Sending..." else "Send OTP",
            onClick = onSendOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = phone.length >= 10 && !isLoading
        )
    }
}

@Composable
private fun OtpStep(
    otp: String,
    onOtpChange: (String) -> Unit,
    phone: String,
    countdown: Int,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verify OTP", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Code sent to +91 $phone",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // OTP boxes
        BasicTextField(
            value = otp,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.focusRequester(focusRequester),
            cursorBrush = SolidColor(BluePrimary),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { index ->
                        val char = otp.getOrNull(index)
                        val isFilled = char != null
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFilled) BluePrimary.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char?.toString() ?: "",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFilled) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (otp == "000000") {
            Text("Demo OTP: use 000000", color = GreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(
            text = "Verify & Continue",
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = otp.length == 6
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (countdown > 0) {
            Text("Resend code in 0:${countdown.toString().padStart(2, '0')}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            TextButton(onClick = onResend) {
                Text("Resend Code", fontWeight = FontWeight.SemiBold, color = BluePrimary)
            }
        }
    }
}

@Composable
private fun RoleSelectStep(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("I am a...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choose your role to continue",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleCard(
                title = "Client",
                subtitle = "Find trusted help",
                emoji = "\uD83D\uDD0D",
                isSelected = selectedRole == UserRole.CLIENT,
                color = BluePrimary,
                onClick = { onRoleChange(UserRole.CLIENT) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                title = "Maid",
                subtitle = "Find work nearby",
                emoji = "\uD83D\uDCBC",
                isSelected = selectedRole == UserRole.MAID,
                color = GreenPrimary,
                onClick = { onRoleChange(UserRole.MAID) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        GradientButton(
            text = "Continue",
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            gradient = if (selectedRole == UserRole.CLIENT)
                Brush.horizontalGradient(listOf(BluePrimary, BluePrimaryDark))
            else
                Brush.horizontalGradient(listOf(GreenPrimary, GreenPrimaryDark))
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
