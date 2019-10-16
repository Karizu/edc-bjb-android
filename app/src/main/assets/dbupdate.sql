1.03.23
1:update iso_additional set service_id = '0' where service_id = 'A54212' and iso_element = 'nama' and influx = 1
1:update iso_additional set iso_seq = 2 where service_id = 'A54212' and iso_element = 'sal_amount' and influx = 1
2:update iso_data set iso_bit_uid = 57, iso_value = '@POSBRI#' where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 37
2:update iso_data set iso_value = '000000' where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 62
2:update iso_data set iso_bit_uid = 22, iso_value = '0901', meta_length = 2 where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 12
2:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id = 'A54A10' and influx = 1 and iso_bit_uid = 13
2:update iso_data set iso_bit_uid = 62, iso_value = '000000', meta_length = 2 where service_id = 'A54A20' and influx = 1 and iso_bit_uid = 37
3:update screen_component set screen_id = '533000X' where screen_id = '541200F' and comp_id = '54119'
3:update screen_component set sequence = 4 where screen_id = '541200F' and comp_id = '54120'
3:update screen_component set sequence = 5 where screen_id = '541200F' and comp_id = '54121'
3:update screen_component set sequence = 6 where screen_id = '541200F' and comp_id = '54114'
3:update screen_component set sequence = 7 where screen_id = '541200F' and comp_id = '54115'
3:update screen_component set sequence = 7 where screen_id = '543120F' and comp_id = '54326'
3:update screen_component set sequence = 8 where screen_id = '543120F' and comp_id = '54327'
3:update screen_component set sequence = 9 where screen_id = '543120F' and comp_id = 'I0010'
3:update screen_component set sequence = 10 where screen_id = '543120F' and comp_id = '54332'
3:update screen_component set sequence = 11 where screen_id = '543120F' and comp_id = '54324'
3:update screen_component set sequence = 12 where screen_id = '543120F' and comp_id = '54325'
3:update screen_component set sequence = 13 where screen_id = '543120F' and comp_id = 'I0011'
3:update screen_component set screen_id = '533000X' where screen_id = '543120F' and comp_id = '5431E'
3:update screen_component set sequence = 14 where screen_id = '543120F' and comp_id = '5431D'
3:update screen_component set sequence = 16 where screen_id = '543120F' and comp_id = '54328'
3:update screen_component set sequence = 17 where screen_id = '543120F' and comp_id = '54329'
3:update screen_component set sequence = 18 where screen_id = '543120F' and comp_id = '5432A'
3:update screen_component set sequence = 19 where screen_id = '543120F' and comp_id = '5431C'
3:update screen_component set sequence = 20 where screen_id = '543120F' and comp_id = '54366'
3:update screen_component set sequence = 21 where screen_id = '543120F' and comp_id = '54331'
3:update component set comp_act = 'nom_tagihan' where comp_id = '54322'
3:update component set comp_act = 'nom_tagihan+nom_admin' where comp_id = '54327'
3:update screen_component set sequence = 7 where screen_id = '543120E' and comp_id = '54326'
3:update screen_component set sequence = 8 where screen_id = '543120E' and comp_id = '54327'
3:update screen_component set sequence = 9 where screen_id = '543120E' and comp_id = 'I0010'
3:update screen_component set sequence = 10 where screen_id = '543120E' and comp_id = '54332'
3:update screen_component set sequence = 11 where screen_id = '543120E' and comp_id = '54324'
3:update screen_component set sequence = 12 where screen_id = '543120E' and comp_id = '54325'
3:update screen_component set sequence = 13 where screen_id = '543120E' and comp_id = 'I0011'
3:delete from screen_component where screen_id = '543120E' and comp_id = '5431E'
3:update screen_component set sequence = 14 where screen_id = '543120E' and comp_id = '5431D'
3:update screen_component set sequence = 16 where screen_id = '543120E' and comp_id = '5431F'
3:update screen_component set sequence = 17 where screen_id = '543120E' and comp_id = '54329'
3:update screen_component set sequence = 18 where screen_id = '543120E' and comp_id = '5432A'
3:update screen_component set sequence = 19 where screen_id = '543120E' and comp_id = '5431C'
3:update screen_component set sequence = 20 where screen_id = '543120E' and comp_id = '54366'
3:update screen_component set sequence = 21 where screen_id = '543120E' and comp_id = '54331'
4:update service_meta set meta_id = 'rnom_admin' where service_id = 'A54312' and influx = 2 and seq = 6
4:update service_meta set meta_id = 'rnom_tagihan' where service_id = 'A54312' and influx = 2 and seq = 8
5:delete from screen_component where screen_id = '541100F' and comp_id = '54119'
5:update screen_component set sequence = 4 where screen_id = '541100F' and comp_id = 'I1004'
5:update screen_component set sequence = 5 where screen_id = '541100F' and comp_id = 'I0004'
6:insert into screen_component (screen_id, comp_id, sequence) select '340000F', comp_id, sequence from screen_component where screen_id = '350000F'
7:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id like 'A545%1' and iso_bit_uid = 12 and influx = 1
7:update iso_data set iso_bit_uid = 52, meta_length = 1 where service_id like 'A545%1' and iso_bit_uid = 13 and influx = 1
8:update iso_data set iso_bit_uid = 25, iso_value = '00', meta_length = 2 where service_id like 'A545%1' and iso_bit_uid = 12 and influx = 1
9:update iso_data set iso_bit_uid = 52, iso_value = null, meta_length = 1 where service_id like 'A545%0' and iso_bit_uid = 12 and influx = 1
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54511', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54521', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54531', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54541', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54551', 'f', 'f')
10:insert into iso_data (channel_uid, iso_bit_uid, meta_type_uid, meta_length, influx, service_id, is_fixed, is_mandatory) values ('40000002', 35, 0, 1, 1, 'A54561', 'f', 'f')
11:update screen set screen_title = 'Shift 3' where screen_id like '3730%'
12:update component set visible = 't' where comp_id = '55620'
13:update screen set screen_title = 'CETAK TOKEN PRABAYAR' where screen_id = '543310F'
14:delete from screen_component where screen_id = '543220F' and comp_id = '54349'
14:update screen_component set sequence=10 where screen_id = '543220F' and comp_id = '54374'
14:update screen_component set sequence=11 where screen_id = '543220F' and comp_id = '54350'
14:update screen_component set sequence=12 where screen_id = '543220F' and comp_id = '54351'
14:update screen_component set sequence=13 where screen_id = '543220F' and comp_id = '54372'
14:update screen_component set sequence=14 where screen_id = '543220F' and comp_id = 'I0010'
14:update screen_component set sequence=15 where screen_id = '543220F' and comp_id = '54373'
14:update screen_component set sequence=16 where screen_id = '543220F' and comp_id = '54332'
14:update screen_component set sequence=17 where screen_id = '543220F' and comp_id = '54353'
14:update screen_component set sequence=18 where screen_id = '543220F' and comp_id = '54354'
15:update iso_additional set iso_seq = 25 where service_id = 'A54322' and influx = 2 and iso_element = 'nom_ppj'
15:update iso_additional set iso_seq = 18 where service_id = 'A54322' and influx = 2 and iso_element = 'rkode11'
15:update iso_additional set iso_seq = 19 where service_id = 'A54322' and influx = 2 and iso_element = 'nom_ppj'
15:update component set comp_act = 'nom_ppj' where comp_id = '54348'
16:update service_meta set meta_id = 'nom_ppj' where meta_id = 'ppj' and service_id = 'A54322' and influx = 2
17:update screen_component set sequence=19 where screen_id = '543220F' and comp_id = '54354'
17:update screen_component set sequence=18 where screen_id = '543220F' and comp_id = '54353'
17:update screen_component set sequence=17 where screen_id = '543220F' and comp_id = '54332'
17:update screen_component set sequence=16 where screen_id = '543220F' and comp_id = '54373'
17:update screen_component set sequence=15 where screen_id = '543220F' and comp_id = 'I0010'
17:update screen_component set sequence=14 where screen_id = '543220F' and comp_id = '54372'
17:update screen_component set sequence=13 where screen_id = '543220F' and comp_id = '54351'
17:update screen_component set sequence=12 where screen_id = '543220F' and comp_id = '54350'
17:update screen_component set sequence=11 where screen_id = '543220F' and comp_id = '54374'
17:insert into screen_component (screen_id, comp_id, sequence) values ('543220F', '54349', 10)
18:update component set visible = 'f' where comp_id in ('20000', '50000', '60000','70000')
19:update screen_component set comp_id='54000' where screen_id='0000000' and sequence=0
19:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90000','0',null,'t','eSamsat','9000000',null,null,null,null,'eSamsat')
19:delete from screen_component where screen_id='5400000'
19:insert into screen_component (screen_id,comp_id,sequence) values ('5400000','90000',0)
20:update component set visible = 't' where comp_id = '54000'
21:insert into screen_component (screen_id, comp_id, sequence) values ('0000000', '54000', 0)
21:update component set visible = 't' where comp_id = '30000'
22:update component set visible = 'f' where comp_id = '30000'
22:INSERT INTO screen (screen_id, screen_type_id, screen_title, version, action_url, screen_title_en, print, print_text) VALUES ('9000000', '1', 'Pembayaran eSamsat', '1.5', 'A90000', 'Pembayaran eSamsat', NULL, NULL)
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90001','2','2','t','Masukan Nomor Polisi Kendaraan : ',null,'t','f',2,10,'Masukan Nomor Polisi Kendaraan : ')
22:insert into screen_component (screen_id,comp_id,sequence) values ('9000000','I0003',0)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9000000','I0001',1)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9000000','90001',2)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9000000','I0004',3)
22:insert into service (service_id,service_name,screen_response,param1,param2,is_to_core) values ('A90000','Inquiry eSamsat','9100000','0100','0110','f')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('track2','A90000',0,1,1,'35')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('pin','A90000',1,1,1,'52')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nopol','A90000',2,1,1,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nopol','A90000',0,1,2,'4')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nama','A90000',1,1,2,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('merek','A90000',2,1,2,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('type','A90000',3,1,2,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('rakit','A90000',4,1,2,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('masa','A90000',5,1,2,'48')
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('rangka','A90000',6,1,4,null)
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('mesin','A90000',7,1,2,12)
22:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nom_bayar','A90000',8,1,2,13)
22:INSERT INTO screen (screen_id, screen_type_id, screen_title, version, action_url, screen_title_en, print, print_text) VALUES ('9100000', '1', 'Pembayaran eSamsat', '1.5', 'A91000', 'Pembayaran eSamsat', NULL, NULL)
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90002','1',null,'t','No Polisi   : ','nopol',null,null,null,null,'No Polisi   : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90003','1',null,'t','Nama        : ','nama',null,null,null,null,'Nama        : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90004','1',null,'t','Merek       : ','merek',null,null,null,null,'Merek       : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90005','1',null,'t','Type        : ','type',null,null,null,null,'Type        : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90006','1',null,'t','Tahun Rakit : ','rakit',null,null,null,null,'Tahun Rakit : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90007','1',null,'t','Masa Pajak  : ','masa',null,null,null,null,'Masa Pajak  : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90008','1',null,'t','No Rangka   : ','rangka',null,null,null,null,'No Rangka   : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90009','1',null,'t','No Mesin    : ','mesin',null,null,null,null,'No Mesin    : ')
22:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90010','1',null,'t','Jml Bayar   : ','nom_bayar',null,null,null,null,'Jml Bayar   : ')
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90002',0)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90003',1)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90004',2)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90005',3)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90006',4)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90007',5)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90008',6)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90009',7)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','90010',8)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','I1004',9)
22:insert into screen_component (screen_id,comp_id,sequence) values ('9100000','I0004',10)
23:update component set comp_content_type = '0' where comp_id = '90001'
24:insert into service (service_id,service_name,screen_response,param1,param2,is_to_core) values ('A91000','Pembayaran eSamsat','910000F','0100','0110','f')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('id','A91000',0,1,1,null)
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('track2','A91000',1,1,3,'35')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('pin','A91000',2,1,3,'52')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nopol','A91000',3,1,3,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nopol','A91000',0,1,2,'4')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nama','A91000',1,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('merek','A91000',2,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('type','A91000',3,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('rakit','A91000',4,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('masa','A91000',5,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('rangka','A91000',6,1,4,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('mesin','A91000',7,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nom_bayar','A91000',8,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nom_admin','A91000',9,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('nom_total','A91000',10,1,2,'48')
24:insert into service_meta(meta_id,service_id,seq,meta_type_id,influx,iso_bit_uid) values ('kode','A91000',11,1,2,'48')
24:INSERT INTO screen (screen_id, screen_type_id, screen_title, version, action_url, screen_title_en, print, print_text) VALUES ('910000F', '0', 'Pembayaran eSamsat', '1.5', 'A91000', 'Pembayaran eSamsat', '2', 'SAM')
24:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90011','1',null,'t','Adm Bank    : ','nom_admin',null,null,null,null,'Adm Bank    : ')
24:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90012','1',null,'t','Jml Total   : ','nom_total',null,null,null,null,'Jml Total   : ')
24:insert into component (comp_id,component_type_id,comp_content_type,visible,comp_lbl,comp_act,mandatory,disabled,min_length,max_length,comp_lbl_en) values ('90013','1',null,'t','Referensi   : ','kode',null,null,null,null,'Referensi   : ')
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90002',0)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90003',1)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90004',2)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90005',3)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90006',4)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90007',5)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90008',6)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90009',7)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90013',8)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90010',9)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90011',10)
24:insert into screen_component (screen_id,comp_id,sequence) values ('910000F','90012',11)
24:update screen set screen_title = 'Tagihan eSamsat' where screen_id = '9100000'
25:update screen set screen_type_id = '1' where screen_id = '910000F'
