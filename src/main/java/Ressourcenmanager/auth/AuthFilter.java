package Ressourcenmanager.auth;

import Ressourcenmanager.user.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PAGES = Set.of(
            "/index.xhtml",
            "/login.xhtml",
            "/register.xhtml"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (isPublic(path)) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        SessionUser authUser = session != null
                ? (SessionUser) session.getAttribute(AuthController.SESSION_USER_KEY)
                : null;

        if (authUser != null) {
            if (isAdminOnly(path) && authUser.getRole() != UserRole.ADMIN) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        httpResponse.sendRedirect(contextPath + "/login.xhtml?redirect=" + encodedPath);
    }

    private boolean isPublic(String path) {
        return PUBLIC_PAGES.contains(path) || path.startsWith("/jakarta.faces.resource/");
    }

    private boolean isAdminOnly(String path) {
        return path.startsWith("/admin/");
    }
}
