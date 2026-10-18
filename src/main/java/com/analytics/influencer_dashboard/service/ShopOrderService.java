package com.analytics.influencer_dashboard.service;

import com.analytics.influencer_dashboard.dto.OrderRow;
import com.analytics.influencer_dashboard.entity.OrderStage;
import com.analytics.influencer_dashboard.entity.ShopOrder;
import com.analytics.influencer_dashboard.repository.ShopOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopOrderService {

    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,]*(\\.\\d+)?");

    private final ShopOrderRepository repository;

    /*
     Standard paste format (one detail per line):
       Name:      Dress:     Color:     Size:     Phone:     Address:     Amount:
     The address may continue on the following lines.
    */
    public ShopOrder createFromText(String rawText, MultipartFile image, String utr) throws IOException {

        Map<String, String> f = new HashMap<>();
        String last = null;
        for (String line : (rawText == null ? "" : rawText).split("\\R")) {
            String t = line.trim();
            if (t.isEmpty()) continue;

            int i = t.indexOf(':');
            String key = i < 0 ? null : canonical(t.substring(0, i).trim().toLowerCase());

            if (key != null) {
                f.put(key, t.substring(i + 1).trim());
                last = key;
            } else if ("address".equals(last)) {
                // continuation of a multi-line address
                f.merge("address", t, (a, b) -> a.isEmpty() ? b : a + ", " + b);
            }
        }

        List<String> missing = new ArrayList<>();
        if (blank(f.get("name"))) missing.add("Name");
        if (blank(f.get("dress"))) missing.add("Dress");
        if (blank(f.get("phone"))) missing.add("Phone");
        if (blank(f.get("amount"))) missing.add("Amount");
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing: " + String.join(", ", missing)
                    + ". Use: Name / Dress / Color / Size / Phone / Address / Amount (one per line).");
        }

        String digits = f.get("phone").replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 12) {
            throw new IllegalArgumentException("Phone number should have 10 digits (or 12 with 91).");
        }

        Integer amount = parseAmount(f.get("amount"));
        if (amount == null) {
            throw new IllegalArgumentException("Amount should be a number greater than 0 (for example: Amount: 1200).");
        }

        ShopOrder order = new ShopOrder();
        order.setCustomerName(clip(f.get("name"), 200));
        order.setDressName(clip(f.get("dress"), 200));
        order.setColor(clip(f.get("color"), 200));
        order.setDressSize(clip(f.get("size"), 200));
        order.setPhone(clip(f.get("phone"), 200));
        order.setAddress(clip(f.get("address"), 500));
        order.setAmount(amount);

        // Paid right away: a payment photo / screenshot is attached
        if (image != null && !image.isEmpty()) {
            String cleanUtr = cleanUtr(utr);
            order.setPaymentImage(image.getBytes());
            order.setPaid(true);
            order.setUtr(cleanUtr);
        }
        return repository.save(order);
    }

    // Paid later: attach the payment photo / screenshot (and UTR) to an existing order
    public void attachPayment(Long id, MultipartFile file, String utr) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please take a photo or upload the payment screenshot.");
        }
        ShopOrder order = find(id);
        if (order.isPaid()) {
            throw new IllegalArgumentException("This order is already paid.");
        }
        String cleanUtr = cleanUtr(utr);
        order.setPaymentImage(file.getBytes());
        order.setPaid(true);
        order.setUtr(cleanUtr);
    }

    public void updateStage(Long id, String stageText) {
        OrderStage stage = OrderStage.fromText(stageText);
        if (stage == null) throw new IllegalArgumentException("Unknown stage.");
        if (repository.updateStage(id, stage) == 0) {
            throw new NoSuchElementException("Order not found");
        }
    }

    public void delete(Long id) {
        if (repository.deleteOrder(id) == 0) {
            throw new NoSuchElementException("Order not found");
        }
    }

    @Transactional(readOnly = true)
    public List<OrderRow> listRows() { return repository.findAllRows(); }

    @Transactional(readOnly = true)
    public long total() { return repository.count(); }

    @Transactional(readOnly = true)
    public long unpaid() { return repository.countByPaidFalse(); }

    @Transactional(readOnly = true)
    public long toOrder() { return repository.countByStage(OrderStage.NEW); }

    @Transactional(readOnly = true)
    public long inProgress() {
        return repository.countByStageIn(List.of(OrderStage.ORDERED, OrderStage.PACKED));
    }

    @Transactional(readOnly = true)
    public long dispatched() { return repository.countByStage(OrderStage.DISPATCHED); }

    @Transactional(readOnly = true)
    public long revenue() {
        Long v = repository.sumPaidAmount();
        return v == null ? 0 : v;
    }

    @Transactional(readOnly = true)
    public byte[] getImage(Long id) {
        return find(id).getPaymentImage();
    }

    // ---------- helpers ----------

    private ShopOrder find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    private static String canonical(String k) {
        return switch (k) {
            case "name", "customer", "customer name" -> "name";
            case "dress", "product", "item" -> "dress";
            case "color", "colour" -> "color";
            case "size", "age" -> "size";
            case "phone", "mobile", "number", "phone number", "contact" -> "phone";
            case "address", "address details", "location", "city", "details" -> "address";
            case "amount", "price", "total" -> "amount";
            default -> null;
        };
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static String clip(String s, int max) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static Integer parseAmount(String s) {
        if (s == null) return null;
        Matcher m = NUMBER.matcher(s);
        if (!m.find()) return null;
        try {
            long v = Math.round(Double.parseDouble(m.group().replace(",", "")));
            return (v > 0 && v <= 10_000_000) ? (int) v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // UTR / transaction reference: optional, 8 to 35 letters or digits
    private static String cleanUtr(String utr) {
        if (utr == null || utr.isBlank()) return null;
        String u = utr.replaceAll("[\\s-]", "").toUpperCase();
        if (!u.matches("[A-Z0-9]{8,35}")) {
            throw new IllegalArgumentException("UTR should be 8 to 35 letters or numbers.");
        }
        return u;
    }
}