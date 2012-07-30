DROP TABLE IF EXISTS task_archive_cf;

/*==============================================================*/
/* Table structure for table task_archive_cf					*/
/*==============================================================*/

CREATE TABLE task_archive_cf(
  id_task INT DEFAULT NULL,
  id_directory INT DEFAULT NULL,
  id_entry_directory INT DEFAULT NULL,
  id_pdfproducer_config INT DEFAULT NULL,
  PRIMARY KEY  (id_task)
  );