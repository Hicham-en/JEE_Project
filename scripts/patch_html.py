import re

with open("src/main/resources/templates/annotator/workspace/solve.html", "r") as f:
    content = f.read()

replacement = """                <label>Classe Choisie:</label>
                <select name="chosenClass" required>
                    <option value="" disabled th:selected="${userAnnotations[tp.id] == null}">Choisir une classe</option>
                    <option th:each="pc : ${dataset.possibleClasses}" 
                            th:value="${pc.libelle}" 
                            th:text="${pc.libelle}"
                            th:selected="${userAnnotations[tp.id] != null and userAnnotations[tp.id] == pc.libelle}">
                    </option>
                </select>"""

content = re.sub(r"""                <label>Classe Choisie:</label>.*?</select>""", replacement, content, flags=re.DOTALL)

with open("src/main/resources/templates/annotator/workspace/solve.html", "w") as f:
    f.write(content)

