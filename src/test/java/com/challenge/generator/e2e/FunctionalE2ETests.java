package com.challenge.generator.e2e;

import com.challenge.generator.expression.api.dto.EmailData;
import com.challenge.generator.expression.api.dto.EmailListResponse;
import com.challenge.generator.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalE2ETests extends BaseTest {

    private record TestCase(String description, String expression, Map<String, String> params, List<String> expectedEmails) {
        public Arguments toArg() {
            return Arguments.of(description, expression, params, expectedEmails);
        }
    }

    private record ErrorTestCase(String description, String expression, Map<String, String> params, String expectedError) {
        public Arguments toArg() {
            return Arguments.of(description, expression, params, expectedError);
        }
    }

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPathTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.e2e.FunctionalE2ETests#happyPathScenarios")
        void generate_emails_successfully(String testName, String expression, Map<String, String> params, List<String> expectedEmails) {
            URI uri = buildUri(expression, params);
            EmailListResponse body = getOk(uri);
            List<EmailData> data = body.data();

            assertNotNull(data);
            assertEquals(expectedEmails.size(), data.size());
            for (int i = 0; i < expectedEmails.size(); i++) {
                assertEquals(expectedEmails.get(i), data.get(i).value(), "Mismatch in test: " + testName);
                assertEquals(data.get(i).value(), data.get(i).id());
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Scenarios")
    class ErrorHandlingTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.e2e.FunctionalE2ETests#errorScenarios")
        void generate_emails_should_fail_with_bad_request(String testName, String expression, Map<String, String> params, String expectedErrorFragment) {
            URI uri = buildUri(expression, params);
            Map<?, ?> body = getBadRequest(uri);
            String error = String.valueOf(body.get("error"));
            assertTrue(error.contains(expectedErrorFragment), "Expected error to contain '" + expectedErrorFragment + "' but was '" + error + "'");
        }
    }

    static Stream<Arguments> happyPathScenarios() {
        return Stream.of(
                basicFunctionTests(),
                advancedChainingTests(),
                edgeCaseTests(),
                ternaryOperatorTests(),
                arithmeticOperatorTests(),
                comparisonOperatorTests(),
                equalsWithArithmeticTests(),
                mapIntegrationTests(),

                complexNestedExpressionsTests(),
                advancedComparisonTests(),
                multiLevelTernaryTests(),
                complexArithmeticComparisonTests(),
                advancedFunctionIntegrationTests(),
                operatorPrecedenceComplexTests(),
                extremeComplexityTests()
        ).flatMap(Function.identity());
    }

    static Stream<Arguments> basicFunctionTests() {
        return Stream.of(
                new TestCase(
                        "Basic substring",
                        "substring(firstName,1,1) & lastName & '@' & domain",
                        Map.of("firstName", "Alice", "lastName", "Smith", "domain", "example.com"),
                        List.of("ASmith@example.com")
                ).toArg(),
                new TestCase(
                        "Zipping and splitting nested",
                        "zip(split(aliases,','),'.',lastName,'@',domain)",
                        Map.of("aliases", "jean,j,jeannot", "lastName", "Mignard", "domain", "peoplespheres.io"),
                        List.of("jean.Mignard@peoplespheres.io", "j.Mignard@peoplespheres.io", "jeannot.Mignard@peoplespheres.io")
                ).toArg()
        );
    }

    static Stream<Arguments> advancedChainingTests() {
        return Stream.of(
                new TestCase(
                        "Chaining: zip with cross_join",
                        "zip(cross_join(split(aliases,','),split('1,2',',')),'.',lastName,'@',domain)",
                        Map.of("aliases", "jean,j", "lastName", "Mignard", "domain", "peoplespheres.io"),
                        List.of("jean1.Mignard@peoplespheres.io", "jean2.Mignard@peoplespheres.io", "j1.Mignard@peoplespheres.io", "j2.Mignard@peoplespheres.io")
                ).toArg(),
                new TestCase(
                        "Chaining: date formatting",
                        "substring(firstName,1,1) & substring(lastName,1,4) & substring(birthDate,3,4) & '.' & department",
                        Map.of("firstName", "Alice", "lastName", "Johnson", "birthDate", "1990-05-15", "department", "IT"),
                        List.of("AJohn90.IT")
                ).toArg(),
                new TestCase(
                        "Chaining: multi-level cross_join",
                        "zip(cross_join(split(teams,';'), split(skills,',')), '.', substring(firstName,1,1))",
                        Map.of("teams", "dev;qa", "skills", "java,python", "firstName", "Maria"),
                        List.of("devjava.M", "devpython.M", "qajava.M", "qapython.M")
                ).toArg()
        );
    }

    static Stream<Arguments> edgeCaseTests() {
        return Stream.of(
                new TestCase(
                        "Edge Case: empty strings in chain",
                        "zip(split(aliases,','), '.', middleName, '.', lastName)",
                        Map.of("aliases", "test,,demo", "middleName", "", "lastName", "User"),
                        List.of("test..User", "..User", "demo..User")
                ).toArg(),
                new TestCase(
                        "Edge Case: single character inputs",
                        "zip(cross_join(split(single,','), name), code)",
                        Map.of("single", "a,b", "name", "X", "code", "Z"),
                        List.of("aXZ", "bXZ")
                ).toArg()
        );
    }

    static Stream<Arguments> ternaryOperatorTests() {
        return Stream.of(
                new TestCase(
                        "Ternary: basic conditional",
                        "firstName & (equals(age, '35') ? '.senior' : '.junior')",
                        Map.of("firstName", "John", "age", "35"),
                        List.of("John.senior")
                ).toArg(),
                new TestCase(
                        "Ternary: nested with function",
                        "firstName & (equals(substring(employeeId,1,1), '1') ? '.mgmt' : '.emp')",
                        Map.of("firstName", "Sarah", "employeeId", "12345"),
                        List.of("Sarah.mgmt")
                ).toArg()
        );
    }

    static Stream<Arguments> arithmeticOperatorTests() {
        return Stream.of(
                new TestCase(
                        "Arithmetic: precedence rules",
                        "base + bonus * multiplier - deduction / factor",
                        Map.of("base", "100", "bonus", "20", "multiplier", "3", "deduction", "40", "factor", "4"),
                        List.of("150")
                ).toArg(),
                new TestCase(
                        "Arithmetic: parentheses grouping",
                        "((base + bonus) * multiplier - deduction) / factor",
                        Map.of("base", "10", "bonus", "5", "multiplier", "4", "deduction", "20", "factor", "5"),
                        List.of("8")
                ).toArg()
        );
    }

    static Stream<Arguments> comparisonOperatorTests() {
        return Stream.of(
                new TestCase(
                        "Comparison: less than with ternary",
                        "firstName & (age < '30' ? '.young' : '.mature')",
                        Map.of("firstName", "John", "age", "25"),
                        List.of("John.young")
                ).toArg(),
                new TestCase(
                        "Comparison: greater than with ternary",
                        "firstName & (age > '65' ? '.senior' : '.working')",
                        Map.of("firstName", "Mary", "age", "70"),
                        List.of("Mary.senior")
                ).toArg(),
                new TestCase(
                        "Comparison: equals with ternary",
                        "firstName & (status = 'VIP' ? '.premium' : '.regular')",
                        Map.of("firstName", "Alice", "status", "VIP"),
                        List.of("Alice.premium")
                ).toArg(),
                new TestCase(
                        "Comparison: not equals with ternary",
                        "firstName & (department != 'HR' ? '.other' : '.hr')",
                        Map.of("firstName", "Bob", "department", "IT"),
                        List.of("Bob.other")
                ).toArg(),
                new TestCase(
                        "Comparison: less than or equal with ternary",
                        "firstName & (score <= '80' ? '.pass' : '.excellent')",
                        Map.of("firstName", "Carol", "score", "80"),
                        List.of("Carol.pass")
                ).toArg(),
                new TestCase(
                        "Comparison: greater than or equal with ternary",
                        "firstName & (experience >= '5' ? '.senior' : '.junior')",
                        Map.of("firstName", "Dave", "experience", "5"),
                        List.of("Dave.senior")
                ).toArg(),
                new TestCase(
                        "Comparison: numeric comparison with decimals",
                        "firstName & (salary > '50000.5' ? '.high' : '.low')",
                        Map.of("firstName", "Eve", "salary", "75000.75"),
                        List.of("Eve.high")
                ).toArg(),
                new TestCase(
                        "Comparison: string comparison lexicographic",
                        "firstName & (lastName < 'M' ? '.early' : '.late')",
                        Map.of("firstName", "Frank", "lastName", "Adams"),
                        List.of("Frank.early")
                ).toArg(),
                new TestCase(
                        "Comparison: arithmetic precedence with comparison",
                        "firstName & (base + bonus > '1000' ? '.bonus' : '.standard')",
                        Map.of("firstName", "Grace", "base", "800", "bonus", "300"),
                        List.of("Grace.bonus")
                ).toArg(),
                new TestCase(
                        "Comparison: chained comparisons with ternary",
                        "firstName & (age >= '18' ? (age < '65' ? '.adult' : '.senior') : '.minor')",
                        Map.of("firstName", "Henry", "age", "45"),
                        List.of("Henry.adult")
                ).toArg()
        );
    }

    static Stream<Arguments> equalsWithArithmeticTests() {
        return Stream.of(
                new TestCase(
                        "Equals with arithmetic: both sides arithmetic expressions",
                        "firstName & (equals('2*3', '18/3') ? '.match' : '.nomatch')",
                        Map.of("firstName", "John"),
                        List.of("John.nomatch")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: complex expressions",
                        "firstName & (equals('10*2-5', '30/2') ? '.equal' : '.unequal')",
                        Map.of("firstName", "Jane"),
                        List.of("Jane.unequal")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: mixed arithmetic and literal",
                        "firstName & (equals('2*3', '6') ? '.correct' : '.wrong')",
                        Map.of("firstName", "Alice"),
                        List.of("Alice.wrong")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: literal and arithmetic",
                        "firstName & (equals('10', '20/2') ? '.same' : '.different')",
                        Map.of("firstName", "Bob"),
                        List.of("Bob.different")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: parentheses in expressions",
                        "firstName & (equals('(2*3)-1', '5') ? '.match' : '.nomatch')",
                        Map.of("firstName", "Charlie"),
                        List.of("Charlie.nomatch")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: decimal arithmetic",
                        "firstName & (equals('2.5*2', '5.0') ? '.decimal' : '.integer')",
                        Map.of("firstName", "Diana"),
                        List.of("Diana.integer")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: subtraction and negative results",
                        "firstName & (equals('3-7', '-4') ? '.negative' : '.positive')",
                        Map.of("firstName", "Eve"),
                        List.of("Eve.positive")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: string concatenation vs arithmetic",
                        "firstName & (equals('2&3', '23') ? '.concat' : '.add')",
                        Map.of("firstName", "Frank"),
                        List.of("Frank.add")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: string concatenation should not equal arithmetic",
                        "firstName & (equals('2&3', '6') ? '.arithmetic' : '.stringconcat')",
                        Map.of("firstName", "Grace"),
                        List.of("Grace.stringconcat")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: division expressions",
                        "firstName & (equals('20/4', '15-10') ? '.equal' : '.notequal')",
                        Map.of("firstName", "Henry"),
                        List.of("Henry.notequal")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: nested parentheses",
                        "firstName & (equals('((3*4)/2)*5', '30') ? '.nested' : '.simple')",
                        Map.of("firstName", "Iris"),
                        List.of("Iris.simple")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: invalid expressions remain as strings",
                        "firstName & (equals('invalid*expression', 'invalid*expression') ? '.string' : '.evaluated')",
                        Map.of("firstName", "Jack"),
                        List.of("Jack.string")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: addition expressions with plus signs",
                        "firstName & (equals('2+3', '5') ? '.addition' : '.notaddition')",
                        Map.of("firstName", "Kate"),
                        List.of("Kate.notaddition")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: complex addition expressions",
                        "firstName & (equals('1+2+3', '6') ? '.multipleplus' : '.noplus')",
                        Map.of("firstName", "Laura"),
                        List.of("Laura.noplus")
                ).toArg(),
                new TestCase(
                        "Equals with arithmetic: mixed addition and other operators",
                        "firstName & (equals('2+3*4', '14') ? '.precedence' : '.noprecedence')",
                        Map.of("firstName", "Mike"),
                        List.of("Mike.noprecedence")
                ).toArg()
        );
    }

    static Stream<Arguments> mapIntegrationTests() {
        return Stream.of(
                new TestCase(
                        "Map: basic map literal with dynamic input",
                        "name:John & '.' & userAge & '.' & status:active",
                        Map.of("userAge", "25"),
                        List.of("John.25.active")
                ).toArg(),
                new TestCase(
                        "Map: map with string literals and dynamic data",
                        "greeting:'Hello' & ' ' & userName & ' - ' & status:active",
                        Map.of("userName", "World"),
                        List.of("Hello World - active")
                ).toArg(),
                new TestCase(
                        "Map: map with zip function and dynamic input",
                        "zip('name:John', userAge, department)",
                        Map.of("userAge", "25", "department", "IT"),
                        List.of("name:John25IT")
                ).toArg(),
                new TestCase(
                        "Map: map with equals function and variables",
                        "name:John & (equals('25', userAge) ? '.match' : '.nomatch')",
                        Map.of("userAge", "25"),
                        List.of("John.match")
                ).toArg(),
                new TestCase(
                        "Map: complex map with dynamic entries",
                        "name:Alice & '.' & role:manager & '.' & department",
                        Map.of("department", "Engineering"),
                        List.of("Alice.manager.Engineering")
                ).toArg(),
                new TestCase(
                        "Map: map with numeric values and variables",
                        "id:123 & '-' & score:95.5 & '-' & userLevel",
                        Map.of("userLevel", "expert"),
                        List.of("123-95.5-expert")
                ).toArg(),
                new TestCase(
                        "Map: map with substring function and variables",
                        "substring(fullName, 1, 4) & '.' & dept:Engineering",
                        Map.of("fullName", "Jonathan"),
                        List.of("Jona.Engineering")
                ).toArg(),
                new TestCase(
                        "Map: map with split and zip functions with dynamic data",
                        "zip(split(skills, ','), '.', role:Developer)",
                        Map.of("skills", "java,kotlin"),
                        List.of("java.Developer", "kotlin.Developer")
                ).toArg(),
                new TestCase(
                        "Map: ternary with map and variable comparison",
                        "userName & (equals('active', userStatus) ? '.verified' : '.pending') & ' - ' & status:active",
                        Map.of("userName", "Sarah", "userStatus", "active"),
                        List.of("Sarah.verified - active")
                ).toArg(),
                new TestCase(
                        "Map: arithmetic with map values and variables",
                        "name:Calculator & ': ' & (baseSalary + bonus:200)",
                        Map.of("baseSalary", "1000"),
                        List.of("Calculator: 1200")
                ).toArg()
        );
    }

    static Stream<Arguments> complexNestedExpressionsTests() {
        return Stream.of(
                new TestCase(
                        "Complex: deeply nested arithmetic with comparisons",
                        "firstName & ((base + bonus * 2) > (salary - deduction * 3) ? '.high' : '.low')",
                        Map.of("firstName", "Alice", "base", "1000", "bonus", "200", "salary", "1800", "deduction", "100"),
                        List.of("Alice.low")
                ).toArg(),
                new TestCase(
                        "Complex: nested ternary with multiple comparisons",
                        "name & (age >= '65' ? '.senior' : (score > '90' ? '.excellent' : (score >= '70' ? '.good' : '.needs_improvement')))",
                        Map.of("name", "Bob", "age", "45", "score", "85"),
                        List.of("Bob.good")
                ).toArg(),
                new TestCase(
                        "Complex: function chaining with arithmetic comparisons",
                        "zip(split(departments, ','), (equals('IT', split(departments, ',')) ? '.tech' : '.other'))",
                        Map.of("departments", "HR,IT,Finance"),
                        List.of("HR.other", "IT.other", "Finance.other")
                ).toArg()
        );
    }

    static Stream<Arguments> advancedComparisonTests() {
        return Stream.of(
                new TestCase(
                        "Advanced: multiple comparison operators in sequence",
                        "name & (age >= '18' & age <= '65' & salary > '50000' ? '.qualified' : '.not_qualified')",
                        Map.of("name", "Carol", "age", "35", "salary", "75000"),
                        List.of("Carol.qualified")
                ).toArg(),
                new TestCase(
                        "Advanced: string vs numeric comparison edge case",
                        "firstName & (equals('007', id) ? '.agent' : (id < '100' ? '.junior' : '.senior'))",
                        Map.of("firstName", "James", "id", "007"),
                        List.of("James.agent")
                ).toArg(),
                new TestCase(
                        "Advanced: decimal precision comparison",
                        "name & (score > '89.999' & score < '90.001' ? '.perfect' : '.close')",
                        Map.of("name", "Diana", "score", "90.0"),
                        List.of("Diana.close")
                ).toArg(),
                new TestCase(
                        "Advanced: negative number comparisons",
                        "name & (balance < '0' ? '.overdrawn' : (balance = '0' ? '.zero' : '.positive'))",
                        Map.of("name", "Eve", "balance", "-150.50"),
                        List.of("Eve.overdrawn")
                ).toArg(),
                new TestCase(
                        "Advanced: mixed arithmetic and comparison with functions",
                        "firstName & (substring(salary, 1, 1) > '5' & salary > '50000' ? '.high_earner' : '.standard')",
                        Map.of("firstName", "Frank", "salary", "65000"),
                        List.of("Frank.high_earner")
                ).toArg()
        );
    }

    static Stream<Arguments> multiLevelTernaryTests() {
        return Stream.of(
                new TestCase(
                        "Multi-Level: 4-level nested ternary with comparisons",
                        "name & (age < '25' ? '.young' : (age < '40' ? (salary > '60000' ? '.prime' : '.standard') : (age < '60' ? '.experienced' : '.senior')))",
                        Map.of("name", "Grace", "age", "32", "salary", "70000"),
                        List.of("Grace.prime")
                ).toArg(),
                new TestCase(
                        "Multi-Level: complex ternary with arithmetic and functions",
                        "firstName & (equals(substring(department, 1, 2), 'IT') ? (salary + bonus > '80000' ? '.tech_lead' : '.tech') : (experience > '10' ? '.senior' : '.junior'))",
                        Map.of("firstName", "Henry", "department", "ITDev", "salary", "70000", "bonus", "15000", "experience", "8"),
                        List.of("Henry.tech_lead")
                ).toArg(),
                new TestCase(
                        "Multi-Level: cascading conditions with mixed operators",
                        "name & (score >= '95' ? '.excellent' : (score >= '85' ? (age < '30' ? '.promising' : '.experienced') : (score >= '70' ? (department = 'Sales' ? '.sales_good' : '.good') : '.needs_improvement')))",
                        Map.of("name", "Iris", "score", "88", "age", "27", "department", "Engineering"),
                        List.of("Iris.promising")
                ).toArg(),
                new TestCase(
                        "Multi-Level: ternary with function results as conditions",
                        "firstName & (equals(substring(role, 1, 3), 'Dev') ? (equals(substring(level, 1, 6), 'Senior') ? '.senior_dev' : '.dev') : (equals(role, 'Manager') ? '.mgmt' : '.other'))",
                        Map.of("firstName", "Jack", "role", "Developer", "level", "Junior"),
                        List.of("Jack.dev")
                ).toArg(),
                new TestCase(
                        "Multi-Level: arithmetic precedence in ternary conditions",
                        "name & ((base + bonus) * multiplier > target ? ((base + bonus) * multiplier > target * 1.5 ? '.exceptional' : '.good') : '.below_target')",
                        Map.of("name", "Kate", "base", "50000", "bonus", "20000", "multiplier", "1.2", "target", "80000"),
                        List.of("Kate.good")
                ).toArg()
        );
    }

    static Stream<Arguments> complexArithmeticComparisonTests() {
        return Stream.of(
                new TestCase(
                        "Arithmetic-Comparison: complex formula with multiple operations",
                        "name & ((base * 1.2 + bonus - tax * 0.3) > (target + adjustment) ? '.above_target' : '.below_target')",
                        Map.of("name", "Laura", "base", "60000", "bonus", "8000", "tax", "15000", "target", "65000", "adjustment", "2000"),
                        List.of("Laura.above_target")
                ).toArg(),
                new TestCase(
                        "Arithmetic-Comparison: percentage calculations with comparisons",
                        "firstName & ((current - previous) / previous * 100 > '10' ? '.improved' : ((current - previous) / previous * 100 < '-5' ? '.declined' : '.stable'))",
                        Map.of("firstName", "Mike", "current", "88000", "previous", "80000"),
                        List.of("Mike.stable")
                ).toArg(),
                new TestCase(
                        "Arithmetic-Comparison: compound interest calculation",
                        "name & (principal * ((1 + rate / 100) * (1 + rate / 100) * (1 + rate / 100)) > '11000' ? '.good_investment' : '.poor_investment')",
                        Map.of("name", "Nina", "principal", "10000", "rate", "5"),
                        List.of("Nina.poor_investment")
                ).toArg(),
                new TestCase(
                        "Arithmetic-Comparison: weighted score calculation",
                        "student & ((math * 0.4 + science * 0.3 + english * 0.3) >= '85' ? '.honors' : ((math * 0.4 + science * 0.3 + english * 0.3) >= '70' ? '.pass' : '.fail'))",
                        Map.of("student", "Oscar", "math", "90", "science", "85", "english", "75"),
                        List.of("Oscar.pass")
                ).toArg(),
                new TestCase(
                        "Arithmetic-Comparison: complex ratio comparisons",
                        "company & ((revenue - costs) / revenue > '0.15' & (revenue - costs) / assets > '0.08' ? '.profitable' : '.struggling')",
                        Map.of("company", "TechCorp", "revenue", "500000", "costs", "400000", "assets", "1000000"),
                        List.of("TechCorp.profitable")
                ).toArg(),
                new TestCase(
                        "Arithmetic-Comparison: nested arithmetic with decimal precision",
                        "name & ((base / 12 + overtime * 1.5) * months >= target - tolerance & (base / 12 + overtime * 1.5) * months <= target + tolerance ? '.on_target' : '.off_target')",
                        Map.of("name", "Paula", "base", "72000", "overtime", "500", "months", "12", "target", "78000", "tolerance", "1000"),
                        List.of("Paula.off_target")
                ).toArg()
        );
    }

    static Stream<Arguments> advancedFunctionIntegrationTests() {
        return Stream.of(
                new TestCase(
                        "Function-Integration: nested functions with comparison logic",
                        "zip(split(teams, ';'), (substring(split(skills, ','), 1, 4) = 'java' ? '.java_team' : '.other_team'))",
                        Map.of("teams", "backend;frontend;devops", "skills", "java,python,docker"),
                        List.of("backend.java_team", "frontend.java_team", "devops.java_team")
                ).toArg(),
                new TestCase(
                        "Function-Integration: cross_join with arithmetic conditions",
                        "zip(cross_join(split(projects, ','), split(priorities, ',')), (equals(split(priorities, ','), 'high') ? '.urgent' : '.normal'))",
                        Map.of("projects", "A,B", "priorities", "high,low"),
                        List.of("Ahigh.normal", "Alow.normal", "Bhigh.normal", "Blow.normal")
                ).toArg(),
                new TestCase(
                        "Function-Integration: substring with complex arithmetic in ternary",
                        "firstName & (substring(employeeId, 1, 1) = '1' ? (salary * bonus / 1000 > '100' ? '.exec' : '.mgmt') : '.staff')",
                        Map.of("firstName", "Quinn", "employeeId", "1001", "salary", "120000", "bonus", "1.2"),
                        List.of("Quinn.exec")
                ).toArg(),
                new TestCase(
                        "Function-Integration: date formatting with comparison logic",
                        "name & substring(birthDate, 7, 4) & (substring(birthDate, 7, 4) > '1990' ? '.millennial' : '.gen_x')",
                        Map.of("name", "Rachel", "birthDate", "15-03-1992"),
                        List.of("Rachel.gen_x")
                ).toArg(),
                new TestCase(
                        "Function-Integration: complex chaining with multiple conditions",
                        "zip(split(departments, ','), (equals(split(budgets, ','), split(departments, ',')) ? '.self_funded' : (split(budgets, ',') > '100000' ? '.well_funded' : '.limited')))",
                        Map.of("departments", "IT,HR,Sales", "budgets", "150000,80000,200000"),
                        List.of("IT.well_funded", "HR.well_funded", "Sales.well_funded")
                ).toArg(),
                new TestCase(
                        "Function-Integration: nested equals with substring and arithmetic",
                        "employee & (equals(substring(department & level, 1, 5), 'ITSen') ? (basePay + bonus > '100000' ? '.senior_tech_high' : '.senior_tech_standard') : '.other')",
                        Map.of("employee", "Sam", "department", "IT", "level", "Senior", "basePay", "85000", "bonus", "20000"),
                        List.of("Sam.senior_tech_high")
                ).toArg()
        );
    }

    static Stream<Arguments> operatorPrecedenceComplexTests() {
        return Stream.of(
                new TestCase(
                        "Precedence: multiplication before addition in comparison",
                        "name & (base + bonus * rate - deduction > target + adjustment * factor ? '.above' : '.below')",
                        Map.of("name", "Tom", "base", "50000", "bonus", "5000", "rate", "2", "deduction", "8000", "target", "55000", "adjustment", "2000", "factor", "1.5"),
                        List.of("Tom.below")
                ).toArg(),
                new TestCase(
                        "Precedence: division before subtraction in ternary",
                        "employee & (total / months - fixed < budget / periods ? '.under_budget' : '.over_budget')",
                        Map.of("employee", "Uma", "total", "120000", "months", "12", "fixed", "2000", "budget", "100000", "periods", "10"),
                        List.of("Uma.under_budget")
                ).toArg(),
                new TestCase(
                        "Precedence: parentheses override in complex expression",
                        "name & ((base + bonus) * (1 + rate / 100) > (target + buffer) * multiplier ? '.qualified' : '.not_qualified')",
                        Map.of("name", "Victor", "base", "60000", "bonus", "10000", "rate", "5", "target", "70000", "buffer", "5000", "multiplier", "1.1"),
                        List.of("Victor.not_qualified")
                ).toArg(),
                new TestCase(
                        "Precedence: comparison before ternary in nested expression",
                        "firstName & (score > average + variance * factor ? (score > excellent - tolerance ? '.outstanding' : '.good') : '.needs_improvement')",
                        Map.of("firstName", "Wendy", "score", "92", "average", "85", "variance", "3", "factor", "2", "excellent", "95", "tolerance", "2"),
                        List.of("Wendy.good")
                ).toArg(),
                new TestCase(
                        "Precedence: string concatenation vs arithmetic precedence",
                        "name & (total & bonus = result ? '.string_match' : (total + bonus = result ? '.arithmetic_match' : '.no_match'))",
                        Map.of("name", "Xavier", "total", "100", "bonus", "50", "result", "150"),
                        List.of("Xavier.arithmetic_match")
                ).toArg(),
                new TestCase(
                        "Precedence: complex mixed operations with multiple levels",
                        "company & (revenue * margin / 100 - costs + savings * rate / 12 > profit * growth + overhead ? '.profitable' : '.break_even')",
                        Map.of("company", "InnovateCorp", "revenue", "1000000", "margin", "25", "costs", "180000", "savings", "50000", "rate", "3", "profit", "60000", "growth", "1.1", "overhead", "10000"),
                        List.of("InnovateCorp.profitable")
                ).toArg()
        );
    }

    static Stream<Arguments> extremeComplexityTests() {
        return Stream.of(
                new TestCase(
                        "Extreme: enterprise calculation with nested conditions",
                        "company & (revenue > '1000000' ? (costs < revenue * 0.7 ? (growth > '10' ? '.excellent' : '.good') : '.poor') : '.startup')",
                        Map.of("company", "MegaCorp", "revenue", "5000000", "costs", "3200000", "growth", "15"),
                        List.of("MegaCorp.excellent")
                ).toArg(),
                new TestCase(
                        "Extreme: multi-function with complex arithmetic and comparisons",
                        "zip(split(departments, ','), (baseSalary + bonus > '100000' & experience >= '5' ? '.senior' : '.junior'))",
                        Map.of("departments", "IT,HR,Sales", "baseSalary", "90000", "bonus", "15000", "experience", "7"),
                        List.of("IT.senior", "HR.senior", "Sales.senior")
                ).toArg(),
                new TestCase(
                        "Extreme: financial analysis with multiple nested ternary",
                        "portfolio & (value > '500000' ? (risk < '0.3' ? (returns > '8' ? '.conservative_winner' : '.conservative') : '.aggressive') : '.small')",
                        Map.of("portfolio", "RetirementFund", "value", "750000", "risk", "0.25", "returns", "9.5"),
                        List.of("RetirementFund.conservative_winner")
                ).toArg(),
                new TestCase(
                        "Extreme: performance evaluation with weighted scores",
                        "employee & ((performance * 0.4 + teamwork * 0.3 + innovation * 0.3) >= '90' ? (tenure > '5' ? '.promote' : '.reward') : '.improve')",
                        Map.of("employee", "JohnDoe", "performance", "95", "teamwork", "88", "innovation", "92", "tenure", "7"),
                        List.of("JohnDoe.promote")
                ).toArg(),
                new TestCase(
                        "Extreme: algorithmic decision with multiple comparisons",
                        "symbol & (price > ma20 & volume > avgVol * 1.5 & rsi < '70' ? (earnings > '0' ? '.buy' : '.hold') : '.sell')",
                        Map.of("symbol", "TECH", "price", "150", "ma20", "145", "volume", "2000000", "avgVol", "1200000", "rsi", "65", "earnings", "2.5"),
                        List.of("TECH.sell")
                ).toArg(),
                new TestCase(
                        "Extreme: supply chain with cascading conditions",
                        "facility & (capacity > demand ? (cost < budget ? (quality >= '95' ? '.optimal' : '.good') : '.expensive') : '.insufficient')",
                        Map.of("facility", "Warehouse_A", "capacity", "10000", "demand", "8500", "cost", "75000", "budget", "80000", "quality", "97"),
                        List.of("Warehouse_A.optimal")
                ).toArg()
        );
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
                new ErrorTestCase(
                        "Empty expression",
                        "",
                        Map.of("lastName", "Doe"),
                        "must not be empty"
                ).toArg(),
                new ErrorTestCase(
                        "Missing input parameters",
                        "zip(split(aliases,','), lastName)",
                        Map.of(),
                        "At least one dynamic input parameter"
                ).toArg(),
                new ErrorTestCase(
                        "Unknown function with suggestion",
                        "splitt(aliases,',') & lastName",
                        Map.of("aliases", "jean,j"),
                        "Unknown function 'splitt'"
                ).toArg(),
                new ErrorTestCase(
                        "Invalid argument count",
                        "split(aliases)",
                        Map.of("aliases", "a,b"),
                        "requires 2 argument(s)"
                ).toArg()
        );
    }
}
