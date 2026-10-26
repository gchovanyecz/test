package hu.gchovanyecz.roombooking.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The API description shown at the top of Swagger UI. Error bodies are documented by
 * {@link hu.gchovanyecz.roombooking.common.web.ProblemDetailOpenApiCustomizer}.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

	private static final String DESCRIPTION = """
			Tárgyalók listázása és foglalása.

			**Foglalási szabályok**

			- A kezdés és a befejezés 15 perces időrácsra essen (pl. 9:00, 9:15).
			- A foglalás legalább 15 perc, legfeljebb 8 óra hosszú lehet.
			- A kezdés nem lehet a múltban.
			- Ugyanabban a tárgyalóban nem lehet átfedő foglalás. Az időintervallum félig nyílt, `[kezdés, befejezés)`:
			  a befejezés pillanatában a tárgyaló már szabad.

			**Hozzáférés**

			A `/api/admin` végpontok autentikáció nélkül érhetők el: a foglalás törlése adminisztrátori feladat.
			""";

	@Bean
	OpenAPI roomBookingOpenApi() {
		return new OpenAPI()
			.info(new Info().title("Tárgyalófoglaló API").version("1.0.0").description(DESCRIPTION))
			.addTagsItem(new Tag().name("Rooms").description("A fix tárgyalólista"))
			.addTagsItem(new Tag().name("Bookings").description("Foglalások leadása és lekérdezése"))
			.addTagsItem(new Tag().name("Admin").description("Adminisztrációs műveletek"));
	}

}
