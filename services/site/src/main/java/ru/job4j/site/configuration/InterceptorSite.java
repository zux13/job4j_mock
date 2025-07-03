package ru.job4j.site.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import ru.job4j.site.dto.UserInfoDTO;
import ru.job4j.site.service.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@Slf4j
public class InterceptorSite implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Получаем userInfo до начала формирования ответа
        UserInfoDTO userInfo = getUserInfo(request);
        // Сохраняем его в request, чтобы использовать в postHandle
        request.setAttribute("userInfo", userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Берём userInfo из request
        var userInfo = (UserInfoDTO) request.getAttribute("userInfo");
        if (modelAndView != null && userInfo != null) {
            modelAndView.addObject("userInfo", userInfo);
        }
    }

    private UserInfoDTO getUserInfo(HttpServletRequest request) {
        // getSession(false): не создаём новую сессию, если её нет
        var session = request.getSession(false);
        if (session == null) {
            return null;
        }
        var token = (String) session.getAttribute("token");
        if (token == null) {
            return null;
        }
        try {
            return authService.userInfo(token);
        } catch (Exception e) {
            log.error("UserInfo data unavailable. {}", e.getMessage());
            return null;
        }
    }
}
