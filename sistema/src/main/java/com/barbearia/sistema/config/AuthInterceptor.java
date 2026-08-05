package com.barbearia.sistema.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var session=request.getSession(false);
        String role=session==null?null:(String)session.getAttribute("role");
        if("ADMIN".equals(role)) return true;
        if(request.getRequestURI().endsWith(".html")){response.sendRedirect("/login.html");return false;}
        response.setStatus(403);response.setContentType("application/json");response.getWriter().write("{\"message\":\"Acesso exclusivo para administradores\"}");return false;
    }
}
