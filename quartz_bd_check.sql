SELECT sched_name, trigger_name, trigger_group, repeat_count, repeat_interval, times_triggered
FROM qrtz_simple_triggers;

select * from qrtz_triggers qt ;

select * from qrtz_job_details qjd ;

select * from qrtz_simprop_triggers qst ;

select * from qrtz_scheduler_state qss ;