package cr.una.reservas_municipales;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.Mockito;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReservasMunicipalesApplicationTests {

	@TestConfiguration
	static class TestMailConfiguration {
		@Bean
		@Primary
		public JavaMailSender javaMailSender() {
			return Mockito.mock(JavaMailSender.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
