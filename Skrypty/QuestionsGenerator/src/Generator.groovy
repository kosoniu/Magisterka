@groovy.lang.Grab('com.itextpdf:itextpdf:5.5.13.2')
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.List
import com.itextpdf.text.ListItem
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import groovy.json.JsonSlurper

class Generator {

    private static def addQuestionsToPDF(source, document, random = false, size) {
        def questionFont = new Font(Font.FontFamily.HELVETICA, 14)
        def answersFont = new Font(Font.FontFamily.HELVETICA, 12)

        if(source.containsKey("title") && source.containsKey("date")) {
            def titleFont = new Font(Font.FontFamily.HELVETICA, 14)

            float[] columnWidths = [1, 1];
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            def title = new Paragraph(source.title, titleFont)

            PdfPCell cell = new PdfPCell(title)
            cell.setColspan(1)
            cell.setBorder(PdfPCell.NO_BORDER)
            table.addCell(cell)

            def date = new Paragraph(source.date, titleFont)
            date.setIndentationLeft(50f)

            cell = new PdfPCell(date)
            cell.setBorder(PdfPCell.NO_BORDER)
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT)
            table.addCell(cell)
            table.setSpacingAfter(10f)

            document.add(table)
        }

        if(random) {
            Collections.shuffle(source.questions)

            if(size > 0) {
                source.questions = source.questions.subList(0, size)
            }
        }

        source.questions.eachWithIndex{item, index ->
            def questionsList = new List(List.ORDERED, List.ALPHABETICAL)
            questionsList.setListSymbol("\u2022")

            def question = new Paragraph(item.question, questionFont)

            question.setSpacingBefore(12f)
            document.add(question)

            item.options.each{option ->
                questionsList.add(new ListItem(option, answersFont))
            }

            document.add(questionsList);
        }
    }

    static def generatePDF(File source, File destination, random = false, size = 0) {
        def document = new Document()
        def jsonSluper = new JsonSlurper()

        PdfWriter.getInstance(document, new FileOutputStream(destination));

        def documentSource = jsonSluper.parse(source)

        println "Generating questions..."

        document.open()
        addQuestionsToPDF(documentSource, document, random, size)
        document.close()

        def counter = 10;
        while(counter > 0) {
            println counter
            counter--
            Thread.sleep(500)
        }

        println "Questions generated..."
    }

}
