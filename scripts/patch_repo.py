import re

with open("src/main/java/com/annotation/domain/repository/AnnotationRepository.java", "r") as f:
    content = f.read()

replacement = """    @Query("SELECT a FROM Annotation a JOIN FETCH a.textPair tp JOIN FETCH a.annotator an JOIN a.task t WHERE t.dataset.id = :datasetId")
    List<Annotation> findByTaskDatasetId(@Param("datasetId") Long datasetId);"""

content = re.sub(r"""    @Query\("SELECT a FROM Annotation a WHERE a.task.dataset.id = :datasetId"\)\s+List<Annotation> findByTaskDatasetId\(@Param\("datasetId"\) Long datasetId\);""", replacement, content, flags=re.DOTALL)

with open("src/main/java/com/annotation/domain/repository/AnnotationRepository.java", "w") as f:
    f.write(content)

