package com.analytics.influencer_dashboard.controller;

import com.analytics.influencer_dashboard.entity.OrderStage;
import com.analytics.influencer_dashboard.service.ShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

/* Every /ecommerce/** URL is already limited to the ECOMMERCE role by RoleInterceptor (WebConfig). */
@Controller
@RequestMapping("/ecommerce")
@RequiredArgsConstructor
public class EcommerceController {

    private final ShopOrderService orders;

    @FunctionalInterface
    private interface Action { void run() throws Exception; }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orders", orders.listRows());
        model.addAttribute("stages", OrderStage.values());
        model.addAttribute("totalOrders", orders.total());
        model.addAttribute("unpaidOrders", orders.unpaid());
        model.addAttribute("newOrders", orders.toOrder());
        model.addAttribute("inProgressOrders", orders.inProgress());
        model.addAttribute("dispatchedOrders", orders.dispatched());
        model.addAttribute("totalRevenue", orders.revenue());
        return "ecommerce-dashboard";
    }

    @PostMapping("/order/save")
    public String save(@RequestParam String rawText,
                       @RequestParam(value = "paymentImage", required = false) MultipartFile paymentImage,
                       @RequestParam(value = "utr", required = false) String utr,
                       RedirectAttributes ra) {
        String result = run(ra, "Order added.", () -> orders.createFromText(rawText, paymentImage, utr));
        if (ra.getFlashAttributes().containsKey("errorMessage")) {
            ra.addFlashAttribute("rawText", rawText); // keep what was pasted so it need not be retyped
        }
        return result;
    }

    @PostMapping("/order/update-payment")
    public String updatePayment(@RequestParam Long orderId,
                                @RequestParam(value = "paymentImage", required = false) MultipartFile file,
                                @RequestParam(value = "utr", required = false) String utr,
                                RedirectAttributes ra) {
        return run(ra, "Payment proof linked.", () -> orders.attachPayment(orderId, file, utr));
    }

    @PostMapping("/order/{id}/stage")
    public String stage(@PathVariable Long id, @RequestParam String stage, RedirectAttributes ra) {
        return run(ra, null, () -> orders.updateStage(id, stage));
    }

    @PostMapping("/order/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        return run(ra, "Order deleted.", () -> orders.delete(id));
    }

    @GetMapping("/payment-image/{id}")
    public ResponseEntity<byte[]> paymentImage(@PathVariable Long id) {
        try {
            byte[] img = orders.getImage(id);
            if (img == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(sniff(img)).body(img);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static MediaType sniff(byte[] b) {
        if (b.length > 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P') return MediaType.IMAGE_PNG;
        if (b.length > 11 && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P')
            return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private String run(RedirectAttributes ra, String ok, Action action) {
        try {
            action.run();
            if (ok != null) ra.addFlashAttribute("successMessage", ok);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (NoSuchElementException e) {
            ra.addFlashAttribute("errorMessage", "Order not found.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Something went wrong. Please try again.");
        }
        return "redirect:/ecommerce/dashboard";
    }
}