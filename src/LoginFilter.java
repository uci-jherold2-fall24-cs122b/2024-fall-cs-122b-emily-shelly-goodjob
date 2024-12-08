import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Check session attributes for 'user' or 'employee' based on URL path
        if (httpRequest.getRequestURI().contains("_dashboard")) {
            if (httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect("login.html?error=employee_only");
                return; // Stop further execution to prevent recursion
            }
        } else if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("login.html");
            return; // Stop further execution to prevent recursion
        }

        // If session attributes are valid, continue to the requested resource
        chain.doFilter(request, response);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        // Check if the URL matches any allowed file types or specific allowed URIs
        if (requestURI == null) {
            return false; // Log the issue here
        }
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        // Allow login-related paths
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("_dashboard/login.html");
        allowedURIs.add("_dashboard/login");
//        allowedURIs.add("_dashboard/*");

        // Allow common static file extensions
        allowedURIs.add(".css");
        allowedURIs.add(".js");
        allowedURIs.add(".png");
        allowedURIs.add(".jpg");
        allowedURIs.add(".ico");
    }

    public void destroy() {
        // ignored.
    }
}
