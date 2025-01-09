INSERT INTO ataque (nombre, tipoAtaque, potencia, descripcion) VALUES
('Pistola agua', 'AGUA', 40, 'Lanza un chorro de agua que daña ligeramente al enemigo.'),
('Hidropulso', 'AGUA', 65, 'Lanza una bola de agua que daña al enemigo.'),
('Cascada', 'AGUA', 80, 'Atrapa al enemigo en una cascada y lo golpea con fuerza.'),
('Surf', 'AGUA', 90, 'Surfea una gran ola que daña gravemente al enemigo.'),
('Hidrobomba', 'AGUA', 110, 'Lanza una potente bomba de agua que daña gravemente al enemigo.'),

('Ascuas', 'FUEGO', 40, 'Lanza brasas por la nariz provocando ligeras quemaduras al enemigo.'),
('Colmillo ígneo', 'FUEGO', 65, 'Muerde al enemigo con sus dientes ardientes.'),
('Puño Fuego', 'FUEGO', 75, 'Golpea al enemigo con su ardiente puño.'),
('Lanzallamas', 'FUEGO', 90, 'Desata un fuego intenso que puede quemar al oponente.'),
('Llamarada', 'FUEGO', 110, 'Crea una estrella de fuego que causa un gran daño al oponente.'),

('Follaje', 'PLANTA', 40, 'Crea una suave tormenta de hojas que atrapa al rival.'),
('Látigo cepa', 'PLANTA', 45, 'Golpea al enemigo con un poderoso latigazo.'),
('Hoja Afilada', 'PLANTA', 60, 'Un ataque de tipo planta que corta al enemigo con hojas afiladas.'),
('Energibola', 'PLANTA', 90, 'Una bola de energía natural que causa daño al enemigo.'),
('Rayo solar', 'PLANTA', 120, 'Un devastador rayo que utiliza la energía del sol para causar un gran daño.'),

('Disparo lodo', 'TIERRA', 50, 'Lanza una bola de lodo a la cara del enemigo.'),
('Terratemblor', 'TIERRA', 60, 'Mueve la tierra desestabilizando al enemigo.'),
('Tierra Viva', 'TIERRA', 90, 'Golpea al enemigo con un poderoso ataque de tipo tierra.'),
('Terremoto', 'TIERRA', 100, 'Potente terremoto con daño masivo.'),

('Impactrueno', 'ELECTRICO', 40, 'Lanza un rayo eléctrico que daña ligeramente al enemigo.'),
('Chispa', 'ELECTRICO', 65, 'Se pega al enemigo y lo electrocuta con una gran chispa.'),
('Puño trueno', 'ELECTRICO', 75, 'Golpea al rival con un poderoso puñetazo eléctrico.'),
('Rayo', 'ELECTRICO', 90, 'Lanza un rayo eléctrico que hace un gran daño.'),
('Trueno', 'ELECTRICO', 110, 'Poderoso trueno que tiene un daño masivo.');


INSERT INTO efecto (nombre, tipoEfecto, multiplicador, descripcion) VALUES
('Tóxico', 'DANO_CONTINUO', 0.1, 'Causa daño continuo al oponente cada turno, reduciendo su vida en un 10%.'),

('Danza Espada', 'SUBIR_ATAQUE_PROPIO', 1.3, 'Aumenta el ataque del Pokémon en un 30%.'),
('Aullido', 'SUBIR_ATAQUE_PROPIO', 1.2, 'Aumenta el ataque de tu Pokémon en un 20%.'),
('Afilar', 'SUBIR_ATAQUE_PROPIO', 1.1, 'Aumenta el ataque de tu Pokémon en un 10%.'),

('Agilidad', 'SUBIR_VELOCIDAD_PROPIO', 1.3, 'Aumenta la velocidad del Pokémon en un 30%.'),

('Reserva', 'SUBIR_DEFENSA_PROPIO', 1.1, 'Aumenta la defensa del Pokémon en un 10%.'),
('Fortaleza', 'SUBIR_DEFENSA_PROPIO', 1.2, 'Aumenta la defensa del Pokémon en un 20%.'),
('Rizo defensa', 'SUBIR_DEFENSA_PROPIO', 1.3, 'Aumenta la defensa del Pokémon en un 30%.'),

('Malicioso', 'BAJAR_DEFENSA_RIVAL', 1/1.3, 'Baja la defensa del Pokémon rival en un 30%.'),
('Látigo', 'BAJAR_DEFENSA_RIVAL', 1/1.2, 'Baja la defensa del Pokémon rival en un 20%.'),

('Ojos llorosos', 'BAJAR_ATAQUE_RIVAL', 1/1.2, 'Baja el ataque del Pokémon rival en un 20%.'),
('Encanto', 'BAJAR_ATAQUE_RIVAL', 1/1.3, 'Baja el ataque del Pokémon rival en un 30%.'),

('Cara susto', 'BAJAR_VELOCIDAD_RIVAL', 1/1.3, 'Baja la velocidad del Pokémon rival en un 30%.');


