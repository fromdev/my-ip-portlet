package com.fromdev.portlets.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.util.PortalUtil;

public class IPAddressUtil {
	public static final String DEFAULT_HEADER_NAMES[] = {
			"x-forwarded-for", "X-Forwarded-For", "Proxy-Client-IP",
			"HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };

	public static String extractHttpUserIp(HttpServletRequest request) throws UnknownHostException {
		if (request == null) {
			return "";
		}
		return extractHttpUserIp(request, DEFAULT_HEADER_NAMES);
	}
	public static String extractHttpUserIp(PortletRequest pReq) throws UnknownHostException {
		if (pReq == null) {
			return "";
		}
		HttpServletRequest request = PortalUtil.getHttpServletRequest(pReq);
		return extractHttpUserIp(request, DEFAULT_HEADER_NAMES);
	}
	public static String extractHttpUserIp(PortletRequest pReq,
			String headerNames[]) throws UnknownHostException {
		if (pReq == null) {
			return "";
		}
		HttpServletRequest request = PortalUtil.getHttpServletRequest(pReq);
		return extractHttpUserIp(request, headerNames);
	}

	public static String extractHttpUserIp(HttpServletRequest request,
			String headerNames[]) throws UnknownHostException {
		if (request == null) {
			return "";
		}


		if (headerNames == null || headerNames.length == 0)
			headerNames = DEFAULT_HEADER_NAMES;
		String extractedIp = null;
		String as[];
		int j = (as = headerNames).length;
		for (int i = 0; i < j; i++) {
			String headerName = as[i];
			String headerValue = request.getHeader(headerName);
//			System.out.println(headerName + " " + headerValue);
			if (headerValue == null)
				continue;
			extractedIp = getOneGoodIp(headerValue.split(","));
//			System.out.println("extractedIp " + extractedIp);
			if (extractedIp != null)
				break;
		}
		return extractedIp == null ? request.getRemoteAddr() : extractedIp;
	}

	public static Map convertToMap(PortletRequest pReq) {
		HttpServletRequest request = PortalUtil.getHttpServletRequest(pReq);
		Map result = new HashMap();
		Enumeration headerNames = request.getHeaderNames();
		if (headerNames != null) {
			String key;
			for (; headerNames.hasMoreElements(); result.put(key,
					request.getHeader(key)))
				key = (String) headerNames.nextElement();
		}
		return result;
	}

	private static String getOneGoodIp(String ipAddressList[])
			throws UnknownHostException {

		for (int i = 0; i < ipAddressList.length; i++) {
			String ipAddress = ipAddressList[i].trim();
//			System.out.println(ipAddress);
			if (ipAddress.length() == 0
					|| (Character.digit(ipAddress.charAt(0), 16) == -1 && ipAddress
							.charAt(0) != ':')) {
				// Not a valid address lets skip it
				continue;
			}

			InetAddress address = InetAddress.getByName(ipAddress);
//			System.out.println("Inet: " + address);

			if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
				continue;
			}
			return address.getHostAddress();
		}

		return null;
	}

}
