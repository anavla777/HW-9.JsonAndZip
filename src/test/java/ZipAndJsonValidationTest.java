import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import model.Instrument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;


public class ZipAndJsonValidationTest {
    private final ClassLoader cl = ZipAndJsonValidationTest.class.getClassLoader();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Check content of CSV inside zip archive")
    void zipValidateCsvTest() throws Exception{
        try (InputStream stream = cl.getResourceAsStream("testArchive.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("csv")){
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> content = csvReader.readAll();
                    assertThat(content)
                            .isNotEmpty()
                            .hasSize(3);
                    assertThat(content.get(1))
                            .isEqualTo(new String[]{"GPU", " ARC A770"});
                }
            }
        }
    }

    @Test
    @DisplayName("Check content of XLSX inside zip archive")
    void zipValidateXlsxTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("testArchive.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("xlsx")){
                    XLS xls = new XLS(zis);
                    final String brand= xls.excel.getSheetAt(1).getRow(3).getCell(0).getStringCellValue();
                    final String model = xls.excel.getSheetAt(1).getRow(3).getCell(1).getStringCellValue();
                    final int coreCount = (int)
                            xls.excel.getSheetAt(1).getRow(3).getCell(2).getNumericCellValue();
                    final int sheetCount = xls.excel.getNumberOfSheets();
                    assertThat(brand)
                            .isEqualTo("Nvidia");
                    assertThat(model)
                            .isEqualTo("RTX 4090");
                    assertThat(coreCount)
                            .isEqualTo(16384);
                    assertThat(sheetCount)
                            .isEqualTo(2);
                }
            }
        }
    }

    @Test
    @DisplayName("Check content of PDF inside zip archive")
    void zipValidatePdfTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("testArchive.zip");
             ZipInputStream zis = new ZipInputStream(stream)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("pdf")){
                    PDF pdf = new PDF(zis);

                    assertThat(pdf.numberOfPages)
                            .isEqualTo(74);
                    assertThat(pdf)
                            .containsExactText("International Software Testing Qualifications Board")
                            .containsExactText("Table of Contents");
                    assertThat(pdf.title)
                            .isEqualTo( "ISTQB Certified Tester - Foundation Level Syllabus v4.0");
                    assertThat(pdf.author).
                            isEqualTo("pwg@istqb.org");
                }
            }
        }
    }

    @Test
    @DisplayName("Check content of Json object")
    void validateJsonTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("Instrument.json");
             Reader reader = new InputStreamReader(stream)) {
            Instrument instrument = mapper.readValue(reader, Instrument.class);
            assertThat(instrument.getExchanges().size())
                    .isEqualTo(3);
            assertThat(instrument.getAdditionalInfo().getTradingEnabledFlag())
                    .isTrue();
            assertThat(instrument.getIsin())
                    .isEqualTo("US0378331005");
            assertThat(instrument.getName())
                    .isEqualTo("AAPL");
            assertThat(instrument.getExchanges())
                    .contains("SSE", "NYSE", "LSE");
        }
    }
}
