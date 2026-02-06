package com.botmanager.bots.thomasnetwork;

import com.botmanager.core.bot.BotConfig;
import com.botmanager.core.flow.FlowContext;
import com.botmanager.core.flow.FlowPlugin;
import com.botmanager.core.flow.FlowState;
import com.botmanager.core.payment.PaymentGateway;
import com.botmanager.core.payment.PaymentRequest;
import com.botmanager.core.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ThomasNetworkFlowPlugin extends FlowPlugin {

    private final PaymentGateway paymentGateway;

    private static final List<BandwidthOption> BANDWIDTH_OPTIONS = List.of(
            new BandwidthOption("basic", "Basic (5 Mbps)", 500, 5),
            new BandwidthOption("standard", "Standard (10 Mbps)", 800, 10),
            new BandwidthOption("premium", "Premium (20 Mbps)", 1200, 20)
    );

    private static final int PRICE_PER_DEVICE = 100;

    @Override
    public void handleAction(String action, Map<String, Object> params, FlowContext context) {
        log.debug("ThomasNetworkFlowPlugin handling action: {}", action);

        switch (action) {
            case "menu.route" -> handleMenuRoute(context);
            case "bandwidth.list" -> handleListBandwidth(context);
            case "bandwidth.validate" -> handleValidateBandwidth(context);
            case "devices.calculate" -> handleCalculateDevices(context);
            case "payments.initiate" -> handleInitiatePayment(context);
            default -> log.warn("Unknown action: {}", action);
        }
    }

    private void handleMenuRoute(FlowContext context) {
        String menuChoice = context.getString("menuChoice");

        if (menuChoice == null) {
            goTo(context, "main_menu");

            return;
        }

        switch (menuChoice.trim()) {
            case "1" -> goTo(context, "bandwidth_list_action");
            case "2" -> goTo(context, "help_message");
            default -> goTo(context, "main_menu");
        }
    }

    private void handleListBandwidth(FlowContext context) {
        StringBuilder messageBuilder = new StringBuilder("Choose your bandwidth:\n\n");
        List<FlowState.ButtonOption> buttons = new ArrayList<>();

        int index = 1;

        for (BandwidthOption option : BANDWIDTH_OPTIONS) {
            messageBuilder.append(index)
                    .append(") ")
                    .append(option.label())
                    .append(" - ")
                    .append(option.basePrice())
                    .append(" XAF/day\n");

            FlowState.ButtonOption button = new FlowState.ButtonOption();
            button.setId(String.valueOf(index));
            button.setTitle(option.label());
            buttons.add(button);

            index++;
        }

        context.set("bandwidthMessage", messageBuilder.toString());
        context.set("bandwidthButtons", buttons);
        goTo(context, "bandwidth_prompt");
    }

    private void handleValidateBandwidth(FlowContext context) {
        String bandwidthChoice = context.getString("bandwidthChoiceInput");

        if (bandwidthChoice == null) {
            goTo(context, "bandwidth_invalid");

            return;
        }

        try {
            int choiceIndex = Integer.parseInt(bandwidthChoice.trim()) - 1;

            if (choiceIndex >= 0 && choiceIndex < BANDWIDTH_OPTIONS.size()) {
                BandwidthOption selected = BANDWIDTH_OPTIONS.get(choiceIndex);
                context.set("bandwidthId", selected.id());
                context.set("bandwidthLabel", selected.label());
                context.set("bandwidthBasePrice", selected.basePrice());
                context.set("bandwidthSpeed", selected.speedMbps());
                goTo(context, "devices_prompt");

                return;
            }
        } catch (NumberFormatException exception) {
            log.debug("Invalid bandwidth choice: {}", bandwidthChoice);
        }

        goTo(context, "bandwidth_invalid");
    }

    private void handleCalculateDevices(FlowContext context) {
        String deviceCountInput = context.getString("deviceCountInput");
        Object basePriceObj = context.get("bandwidthBasePrice");

        if (deviceCountInput == null || basePriceObj == null) {
            goTo(context, "devices_invalid");

            return;
        }

        try {
            int deviceCount = Integer.parseInt(deviceCountInput.trim());

            if (deviceCount < 1 || deviceCount > 10) {
                context.set("devicesError", "Please enter a number between 1 and 10");
                goTo(context, "devices_invalid");

                return;
            }

            int basePrice = basePriceObj instanceof Number ? ((Number) basePriceObj).intValue() : 0;
            int totalPrice = basePrice + (deviceCount * PRICE_PER_DEVICE);

            context.set("deviceCount", deviceCount);
            context.set("totalPrice", totalPrice);
            context.set("paymentAmount", totalPrice);
            context.set("paymentCurrency", "XAF");

            String summary = String.format(
                    "Order Summary:\n- Bandwidth: %s\n- Devices: %d\n- Total: %d XAF",
                    context.getString("bandwidthLabel"),
                    deviceCount,
                    totalPrice
            );
            context.set("orderSummary", summary);

            goTo(context, "payment_confirm");
        } catch (NumberFormatException exception) {
            context.set("devicesError", "Please enter a valid number");
            goTo(context, "devices_invalid");
        }
    }

    private void handleInitiatePayment(FlowContext context) {
        BotConfig botConfig = (BotConfig) context.get("botConfig");
        String customerPhone = context.getString("customerPhone");
        String bandwidthId = context.getString("bandwidthId");
        Object deviceCountObj = context.get("deviceCount");
        Object amountObj = context.get("paymentAmount");
        String currency = context.getString("paymentCurrency");

        int amount = amountObj instanceof Number ? ((Number) amountObj).intValue() : 0;
        int deviceCount = deviceCountObj instanceof Number ? ((Number) deviceCountObj).intValue() : 1;

        String reference = botConfig.getBotId() + "-" + bandwidthId + "-" + System.currentTimeMillis();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("bandwidthId", bandwidthId);
        metadata.put("deviceCount", deviceCount);
        metadata.put("customerPhone", customerPhone);

        PaymentRequest request = PaymentRequest.builder()
                .botId(botConfig.getBotId())
                .amount(amount)
                .currency(currency)
                .phoneNumber(customerPhone)
                .reference(reference)
                .description("Network Access - " + bandwidthId + " - " + deviceCount + " devices")
                .metadata(metadata)
                .build();

        PaymentResult result = paymentGateway.initiatePayment(request);

        if (result.isSuccess()) {
            context.set("transactionId", result.getTransactionId());
            context.set("paymentStatus", result.getStatus().getValue());
            goTo(context, "payment_pending");
        } else {
            context.set("paymentError", result.getErrorMessage());
            goTo(context, "payment_failed");
        }
    }

    private record BandwidthOption(String id, String label, int basePrice, int speedMbps) {}

}
