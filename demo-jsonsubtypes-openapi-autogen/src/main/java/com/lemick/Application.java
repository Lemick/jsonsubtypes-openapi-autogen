package com.lemick;

import com.lemick.model.Instrument;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Operation(description = "Create instrument, the Instrument subtypes available are explicited thanks to the " +
            "discriminator annotations added at compile time")
    @PutMapping
    public Instrument createInstrument(@Valid Instrument instrument) {
        return instrument;
    }

    @Operation(hidden = true)
    @GetMapping("/")
    public String apiDoc() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>ReDoc</title>\n" +
                "    <!-- needed for adaptive design -->\n" +
                "    <meta charset=\"utf-8\"/>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700\" rel=\"stylesheet\">\n" +
                "\n" +
                "    <!--\n" +
                "    ReDoc doesn't change outer page styles\n" +
                "    -->\n" +
                "    <style>\n" +
                "      body {\n" +
                "        margin: 0;\n" +
                "        padding: 0;\n" +
                "      }\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <redoc spec-url='./v3/api-docs'></redoc>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/redoc@next/bundles/redoc.standalone.js\"> </script>\n" +
                "  </body>\n" +
                "</html>;";
    }
}