def questions = new File("../resources/questions.json")
def destination = new File("../resources/generated-questions.pdf")

def random = false;
def size = 0;

args.eachWithIndex{ String entry, int i ->
    if(i == 0 && args[0]) {
        questions = new File(args[0])
    }

    if(i == 1 && args[1]) {
        destination = new File(args[1])
    }

    if(i == 2 && args[2]) {
        random = Boolean.parseBoolean(args[2])
    }

    if(i == 3 && args[3]) {
        size = Integer.parseInt(args[3])
    }
}

Generator.generatePDF(questions, destination, random, size)