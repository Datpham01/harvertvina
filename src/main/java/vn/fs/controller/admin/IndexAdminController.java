package vn.fs.controller.admin;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.fs.entities.User;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.UserRepository;


@Controller
@RequestMapping("/admin")
public class IndexAdminController{
	
	@Autowired
	UserRepository userRepository;
	
	 @Autowired
	 private OrderRepository orderRepository;
	 
	 @Autowired
	OrderDetailRepository orderDetailRepository;

	@ModelAttribute(value = "user")
	public User user(Model model, Principal principal) {
		if (principal != null) {
			if (principal instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) principal;
				Map<String, Object> attributes = oAuth2Token.getPrincipal().getAttributes();
				String email = (String) attributes.get("email");

				if (email != null) {
					User user = userRepository.findByEmail(email);
					if (user != null) {
						model.addAttribute("user", user);
						return user;
					}
				}
			} else {
				String email = principal.getName();
				User user = userRepository.findByEmail(email);
				if (user != null) {
					model.addAttribute("user", user);
					return user;
				}
			}
		}

		User anonymousUser = new User();
		model.addAttribute("user", anonymousUser);
		return anonymousUser;
	}

	@GetMapping(value = "/home")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
	public String showDashboard(Model model) {
//		 List<Object[]> monthlyStats = orderDetailRepository.repoWhereMonth();
//		model.addAttribute("monthlyStats", monthlyStats);
		
		List<Object[]> monthlyStats = orderDetailRepository.repoWhereMonth();
	    // Chuyển đổi danh sách monthlyStats thành mảng JSON
	    ObjectMapper objectMapper = new ObjectMapper();
	    String monthlyStatsJson;
	    try {
	        monthlyStatsJson = objectMapper.writeValueAsString(monthlyStats);
	    } catch (JsonProcessingException e) {
	        // Xử lý ngoại lệ nếu có
	        monthlyStatsJson = "[]"; // Trả về một mảng JSON rỗng nếu có lỗi
	    }
	    // Truyền mảng JSON vào model
	    model.addAttribute("monthlyStats", monthlyStatsJson);
		
		
        model.addAttribute("totalRevenue", orderRepository.findTotalRevenue());
        model.addAttribute("successfulOrders", orderRepository.countSuccessfulOrders());
        model.addAttribute("cancelledOrders", orderRepository.countCancelledOrders());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("newOrders", orderRepository.countNewOrders());

        return "admin/index"; // Tên của file template trong thư mục templates/admin
    }
}
