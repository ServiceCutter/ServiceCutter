insert into coupling_criterion (id, name, type) values (1, 'Identity & Lifecycle Commonality', 'PROXIMITY');
insert into coupling_criterion (id, name, type) values (2, 'Semantic Proximity', 'PROXIMITY');
insert into coupling_criterion (id, name, type) values (3, 'Security Constraint', 'SEPARATION');
insert into coupling_criterion (id, name, type) values (4, 'Security Criticality', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (5, 'Security Context Distinction', 'PROXIMITY');
insert into coupling_criterion (id, name, type) values (6, 'Resilience', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (7, 'Volatility', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (8, 'Consistency', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (9, 'Storage Similarity', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (10, 'Network Traffic Similarity', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (11, 'Change Similarity', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (12, 'Predefined Service Constraint', 'EXCLUSIVE');
insert into coupling_criterion (id, name, type) values (13, 'Mutability', 'DISTANCE');
insert into coupling_criterion (id, name, type) values (14, 'Responsibility', 'PROXIMITY');

-- description
update coupling_criterion set description = 'Data which belong to the same identity and therefore shares a common lifecycle.' where id = 1;
update coupling_criterion set description = 'A field A is semantically close to field B. Semantic proximity originates from coherent field updates (e.g. in a transaction / common use case) or aggregations in UML class diagram.' where id = 2;
update coupling_criterion set description = 'Data which mustn’t be kept in the same service.' where id = 3;
update coupling_criterion set description = 'Criticality in a security context of a given field.' where id = 4;
update coupling_criterion set description = 'One service ideally serves a single role. If one services is accessed by multiple roles with different security contexts, an authorization system needs to be implemented and maintained.' where id = 5;
update coupling_criterion set description = 'Data has varying availability constraints. Some parts are critical while others can be unavailable for some time.' where id = 6;
update coupling_criterion set description = 'Some data is hardly updated. Some data is updated very often.' where id = 7;
update coupling_criterion set description = 'Some data loses its value if not kept consistent together while other data is more tolerant to inconsistencies.' where id = 8;
update coupling_criterion set description = 'Storage that is required to persist all instances of a data field.' where id = 9;
update coupling_criterion set description = 'Volume of data transferred on the network. This information is defined by how often an instance of a field is read or written, how many instances of the field exist and the size of the field.' where id = 10;
update coupling_criterion set description = 'How often do Change Requests have to be implemented in this area.' where id = 11;
update coupling_criterion set description = 'There might be different reasons why some parts forcefully needs to be modelled in the same service.' where id = 12;
update coupling_criterion set description = 'Some data can be defined as immutable, meaning that it will not be affected by any change after the time of creation.' where id = 13;
update coupling_criterion set description = 'Data is usually governed, maintained or produced by a single person, a role or a department of a company.' where id = 14;

-- decomposition impact
update coupling_criterion set decomposition_impact = 'Model in same service.' where id = 1;
update coupling_criterion set decomposition_impact = 'Gather together those things that change for the same reason, and separate those things that change for different reasons. (Robert C. Martin)' where id = 2;
update coupling_criterion set decomposition_impact = 'Model in different services.' where id = 3;
update coupling_criterion set decomposition_impact = 'Do not model requirements with different criticality in same service.' where id = 4;
update coupling_criterion set decomposition_impact = 'Do not mix different security contexts in a single service.' where id = 5;
update coupling_criterion set decomposition_impact = 'Do not model requirements with different requirements in same service.' where id = 6;
update coupling_criterion set decomposition_impact = 'Do not model data with different requirements in same service.' where id = 7;
update coupling_criterion set decomposition_impact = 'Do not model requirements with different requirements in same service.' where id = 8;
update coupling_criterion set decomposition_impact = 'Do not model requirements with different requirements in same service.' where id = 9;
update coupling_criterion set decomposition_impact = 'Model data which is regularly accessed within the same services to avoid network traffic' where id = 10;
update coupling_criterion set decomposition_impact = 'Do not model requirements with different requirements in same service' where id = 11;
update coupling_criterion set decomposition_impact = 'Respect the constraints in the modelling algorithm.' where id = 12;
update coupling_criterion set decomposition_impact = 'Immutable data are well suited for published language and communication between services. So if data fields have high coupling but are immutable, they might still be splitted across services if this generates other advantages.' where id = 13;
update coupling_criterion set decomposition_impact = 'Services should reflect the organization of a company.' where id = 13;


-- 1
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (1, 1, 1, 'Same Entity');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (2, 0, 1, 'Composition');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (3, 0, 1, 'Inheritance');
-- 2
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (4, 0, 2, 'Aggregation');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (5, 0, 2, 'Shared Field Access');
-- 3
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (6, 1, 3, 'Separation Contraint');
-- 4
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (7, 1, 4, 9, 'High');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (8, 1, 4, 3, 'Medium');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (9, 1, 4, 1, 'Low');
-- 5
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (10, 1, 5, 'Security Context');
-- 6
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (11, 1, 6, 9, 'Critical');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (12, 1, 6, 4, 'Normal');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (13, 1, 6, 1, 'Low');
-- 7
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (14, 1, 7, 8, 'Often');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (15, 1, 7, 3, 'Regularly');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (16, 1, 7, 1, 'Rarely');
-- 8
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (17, 1, 8, 8, 'High');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (18, 1, 8, 2, 'Eventually');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (19, 1, 8, 2, 'Weak');
-- 9
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (20, 1, 9, 1, 'Small (KB)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (21, 1, 9, 2, 'Medium (MB)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (22, 1, 9, 4, 'Large (GB)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (23, 1, 9, 9, 'Huge (TB)');
-- 10
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (24, 1, 10, 1, 'Low (1/d)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (25, 1, 10, 2, 'Medium (1/min)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (26, 1, 10, 4, 'High (1/s)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (27, 1, 10, 9, 'Huge (100/s)');
-- 11
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (28, 1, 11, 9, 'Often (1/month)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (29, 1, 11, 3, 'Rarely (1/year)');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, weight, name) values (30, 1, 11, 1, 'Never');
-- 12
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (31, 1, 12, 'Predefined Service');
-- 13
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (32, 1, 13, 'Mutable');
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (33, 1, 13, 'Immutable');
-- 14
insert into coupling_criteria_variant (id, mono_coupling, coupling_criterion_id, name) values (34, 1, 14, 'Predefined Service');


