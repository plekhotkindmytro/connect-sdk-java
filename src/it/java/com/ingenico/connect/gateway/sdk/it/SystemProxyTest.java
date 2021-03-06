package com.ingenico.connect.gateway.sdk.it;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import com.ingenico.connect.gateway.sdk.java.Client;
import com.ingenico.connect.gateway.sdk.java.CommunicatorConfiguration;
import com.ingenico.connect.gateway.sdk.java.Factory;
import com.ingenico.connect.gateway.sdk.java.domain.services.ConvertAmount;
import com.ingenico.connect.gateway.sdk.java.merchant.services.ConvertAmountParams;

public class SystemProxyTest extends ItTest {

	/**
	 * Smoke test for using a proxy configured through system properties.
	 */
	@Test
	public void test() throws URISyntaxException, IOException {

		final boolean[] authenticationCalled = { false };

		if (System.getProperty("http.proxyHost") == null) {
			System.setProperty("http.proxyHost", "proxy.isaac.local");
		}
		if (System.getProperty("http.proxyPort") == null) {
			System.setProperty("http.proxyPort", "3128");
		}
		if (System.getProperty("https.proxyHost") == null) {
			System.setProperty("https.proxyHost", "proxy.isaac.local");
		}
		if (System.getProperty("https.proxyPort") == null) {
			System.setProperty("https.proxyPort", "3128");
		}
		final String username = System.getProperty("http.proxyUser", "global-collect");
		final String password = System.getProperty("http.proxyPass", "global-collect");

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				authenticationCalled[0] = true;

				return new PasswordAuthentication(username, password.toCharArray());
			}
		});

		ConvertAmountParams request = new ConvertAmountParams();
		request.setAmount(123L);
		request.setSource("USD");
		request.setTarget("EUR");

		CommunicatorConfiguration configuration = getCommunicatorConfiguration()
				.withProxyConfiguration(null);

		Client client = Factory.createClient(configuration);
		try {
			ConvertAmount response = client.merchant("9991").services().convertAmount(request);

			Assert.assertNotNull(response.getConvertedAmount());

		} finally {
			client.close();
		}

		// for https, authentication may not be required
		if ("http".equalsIgnoreCase(configuration.getApiEndpoint().getScheme())) {
			Assert.assertTrue("getPasswordAuthentication() should have been called", authenticationCalled[0]);
		}
	}
}
