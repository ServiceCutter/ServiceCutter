insert into coupling_criterion (id, code, name, type) values ( 1, 'CC-1', 'Identity & Lifecycle Commonality', 'COHESIVENESS');
insert into coupling_criterion (id, code, name, type) values ( 2, 'CC-2', 'Semantic Proximity', 'COHESIVENESS');
insert into coupling_criterion (id, code, name, type) values (14, 'CC-3', 'Shared Owner', 'COHESIVENESS');
insert into coupling_criterion (id, code, name, type) values (11, 'CC-4', 'Structural Volatility', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values (15, 'CC-5', 'Latency', 'COHESIVENESS');
insert into coupling_criterion (id, code, name, type) values ( 8, 'CC-6', 'Consistency Criticality', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values ( 6, 'CC-7', 'Availability Criticality', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values ( 7, 'CC-8', 'Content Volatility', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values (16, 'CC-9', 'Consistency Constraint', 'CONSTRAINTS');
--insert into coupling_criterion (id, code, name, type) values (13, 'CC-10', 'Mutability', 'COMMUNICATION');
insert into coupling_criterion (id, code, name, type) values ( 9, 'CC-11', 'Storage Similarity', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values (12, 'CC-12', 'Predefined Service Constraint', 'CONSTRAINTS');
--insert into coupling_criterion (id, code, name, type) values (10, 'CC-13', 'Network Traffic Similarity', 'COMMUNICATION');
insert into coupling_criterion (id, code, name, type) values ( 5, 'CC-14', 'Security Contextuality', 'COHESIVENESS');
insert into coupling_criterion (id, code, name, type) values ( 4, 'CC-15', 'Security Criticality', 'COMPATIBILITY');
insert into coupling_criterion (id, code, name, type) values ( 3, 'CC-16', 'Security Constraint', 'CONSTRAINTS');

-- description
-- CC-1
update coupling_criterion set description = 'Nanoentities that belong to the same identity and therefore share a common lifecycle.' where id = 1;
-- CC-2
update coupling_criterion set description = 'Two nanoentities are semantically proximate when they have a semantic connection given by the business domain. The strongest indicator for semantic proximity is coherent access on nanoentities within the same use case.' where id = 2;
-- CC-3
update coupling_criterion set description = 'The same person, role or department is responsible for a group of nanoentities. Service decomposition should try to keep entities with the same responsible role together while not mixing entities with different responsible instances in one service.' where id = 14;
-- CC-4
update coupling_criterion set description = 'How often change requests need to be implemented affecting nanoentities.' where id = 11;
-- CC-5
update coupling_criterion set description = 'Groups of nanoentities with high performance requirements for a specific user request. These nanoentities should be modelled in the same service to avoid remote calls.' where id = 15;
-- CC-6
update coupling_criterion set description = 'Some data such as financial records loses its value in case of inconsistencies while other data is more tolerant to inconsistencies.' where id = 8;
-- CC-7
update coupling_criterion set description = 'Nanoentities have varying availability constraints. Some are critical while others can be unavailable for some time. As providing high availability comes at a cost, nanoentities classified with different characteristics should not be composed in the same service.' where id = 6;
-- CC-8
update coupling_criterion set description = 'A nanoentity can be classified by its volatility which defines how frequent it is updated. Highly volatile and more stable nanoentities should be composed in different services.' where id = 7;
-- CC-9
update coupling_criterion set description = 'A group of nanoentities that have a dependent state and therefore need to be kept consistent to each other.' where id = 16;
-- CC-10
--update coupling_criterion set description = 'Immutable information is much simpler to manage in a distributed system than mutable objects. Immutable nanoentities are therefore good candidates for the published language shared between two services. Service decomposition should be done in a way that favors sharing immutable nanoentities over mutable ones.' where id = 13;
-- CC-11
update coupling_criterion set description = 'Storage that is required to persist all instances of a nanoentity.' where id = 9;
-- CC-12
update coupling_criterion set description = 'There might be the following reasons why some nanoentities forcefully need to be modelled in the same service: Technological optimizations or Legacy systems' where id = 12;
-- CC-13
--update coupling_criterion set description = 'Service decomposition has a significant impact on network traffic, depending on which nanoentities are shared between services and how often. Small and less frequently accessed nanoentities are better suited to be shared between services.' where id = 10;
-- CC-14
update coupling_criterion set description = 'A security role is allowed to see or process a group of nanoentities. Mixing security contexts in one service complicates authentication and authorization implementations.' where id = 5;
-- CC-15
update coupling_criterion set description = 'Criticality of an nanoentity in case of data loss or a privacy violation. Represents the reputational or financial damage when the information is disclosed to unauthorized parties. As high security criticality comes at a cost, nanoentities classified with different characteristics should not be composed in the same service.' where id = 4;
-- CC-16
update coupling_criterion set description = 'Groups of nanoentities are semantically related but must not reside in the same service in order to satisfy information security requirements. This restriction can be established by an external party such as a certification authority or an internal design team.' where id = 3;

-- CC-4 Structural Volatility
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (28, 11, 10, 'Often', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (29, 11, 4, 'Normal', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (30, 11, 0, 'Rarely', 0);

-- CC-6 Consistency
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (17, 8, 10, 'High', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (18, 8, 4, 'Eventually', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (19, 8, 0, 'Weak', 0);

-- CC-7 Availability
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (11, 6, 10, 'Critical', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (12, 6, 4, 'Normal', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (13, 6, 0, 'Low', 0);

-- CC-8 Content Volatility
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (14, 7, 10, 'Often', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (15, 7, 5, 'Regularly', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (16, 7, 0, 'Rarely', 0);

-- CC-11 Storage Similarity
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (20, 9, 0, 'Tiny', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (21, 9, 3, 'Normal', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (22, 9, 10, 'Huge', 0);

-- CC-15 Security Criticality
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (7, 4, 10, 'Critical', 0);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (8, 4, 3, 'Internal', 1);
insert into cc_characteristic (id, coupling_criterion_id, weight, name, is_default) values (9, 4, 0, 'Public', 0);

