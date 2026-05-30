import re

with open("src/main/resources/templates/annotator/workspace/solve.html", "r") as f:
    content = f.read()

replacement = """            <p th:if="${tp.text1 != null}"><strong>Texte 1:</strong> <span th:text="${tp.text1}"></span></p>
            <p th:if="${tp.text2 != null}"><strong>Texte 2:</strong> <span th:text="${tp.text2}"></span></p>"""

content = re.sub(r"""            <p><strong>Texte 1:</strong> <span th:text="\$\{tp.text1\}"></span></p>\s+<p><strong>Texte 2:</strong> <span th:text="\$\{tp.text2\}"></span></p>""", replacement, content, flags=re.DOTALL)

with open("src/main/resources/templates/annotator/workspace/solve.html", "w") as f:
    f.write(content)

