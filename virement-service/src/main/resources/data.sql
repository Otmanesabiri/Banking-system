INSERT INTO virements (source_account, destination_account, montant, type, date_execution, statut, motif) VALUES
 ('ACC001', 'RIB0001', 1200.00, 'NATIONAL', CURRENT_TIMESTAMP, 'EXECUTE', 'Loyer'),
 ('ACC002', 'RIB0002', 250.00, 'INTERNATIONAL', CURRENT_TIMESTAMP, 'EN_COURS', 'Fournisseur'),
 ('ACC003', 'RIB0003', 500.00, 'PERMANENT', CURRENT_TIMESTAMP, 'EN_ATTENTE', 'Epargne');
