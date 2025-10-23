package cr.una.reservas_municipales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReservasMunicipalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasMunicipalesApplication.class, args);
	}

}
