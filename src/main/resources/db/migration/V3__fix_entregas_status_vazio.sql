-- Corrige rows de entregas com status NULL ou vazio (inseridos antes do @Enumerated(STRING))
UPDATE entregas SET status = 'PENDENTE' WHERE status IS NULL OR status = '';
