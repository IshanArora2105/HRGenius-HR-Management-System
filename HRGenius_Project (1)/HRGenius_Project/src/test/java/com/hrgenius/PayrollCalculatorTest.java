package com.hrgenius;
import com.hrgenius.service.PayrollCalculator; import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class PayrollCalculatorTest {
 @Test void reproducesDocumentedFirstExample(){var r=PayrollCalculator.calculate(new BigDecimal("50000"),new BigDecimal("20000"),new BigDecimal("5000"),new BigDecimal("200"),new BigDecimal("625"));assertEquals(new BigDecimal("68175.00"),r.net());}
 @Test void reproducesDocumentedSecondExample(){var r=PayrollCalculator.calculate(new BigDecimal("30000"),new BigDecimal("12000"),new BigDecimal("3000"),new BigDecimal("200"),BigDecimal.ZERO);assertEquals(new BigDecimal("41200.00"),r.net());}
}
