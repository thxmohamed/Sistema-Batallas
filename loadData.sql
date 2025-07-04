INSERT INTO ataque (nombre, tipo_ataque, potencia, descripcion) VALUES
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
('Látigo cepa', 'PLANTA', 55, 'Golpea al enemigo con un poderoso latigazo.'),
('Hoja Afilada', 'PLANTA', 75, 'Un ataque de tipo planta que corta al enemigo con hojas afiladas.'),
('Energibola', 'PLANTA', 90, 'Una bola de energía natural que causa daño al enemigo.'),
('Rayo solar', 'PLANTA', 120, 'Un devastador rayo que utiliza la energía del sol para causar un gran daño.'),

('Disparo lodo', 'TIERRA', 50, 'Lanza una bola de lodo a la cara del enemigo.'),
('Terratemblor', 'TIERRA', 60, 'Mueve la tierra desestabilizando al enemigo.'),
('Tierra Viva', 'TIERRA', 90, 'Golpea al enemigo con un poderoso ataque de tipo tierra.'),
('Terremoto', 'TIERRA', 100, 'Potente terremoto con daño masivo.'),
('Mil flechas', 'TIERRA', 120, 'Dispara múltiples flechas de tierra que atraviesan al enemigo.'),

('Impactrueno', 'ELECTRICO', 40, 'Lanza un rayo eléctrico que daña ligeramente al enemigo.'),
('Chispa', 'ELECTRICO', 65, 'Se pega al enemigo y lo electrocuta con una gran chispa.'),
('Puño trueno', 'ELECTRICO', 75, 'Golpea al rival con un poderoso puñetazo eléctrico.'),
('Rayo', 'ELECTRICO', 90, 'Lanza un rayo eléctrico que hace un gran daño.'),
('Trueno', 'ELECTRICO', 110, 'Poderoso trueno que tiene un daño masivo.'),

('Placaje', 'NORMAL', 50, 'Un ataque básico donde el Pokémon embiste al enemigo con todo su cuerpo.'),
('Patada', 'NORMAL', 60, 'Una potente patada que impacta directamente al oponente.'),
('Golpe cuerpo', 'NORMAL', 85, 'Un ataque físico donde todo el cuerpo se usa como proyectil.'),
('Megapuño', 'NORMAL', 100, 'Un puñetazo tremendamente poderoso que causa gran daño.'),
('Gigaimpacto', 'NORMAL', 140, 'Un ataque explosivo que causa un gran daño.');


INSERT INTO efecto (nombre, tipo_efecto, multiplicador, descripcion) VALUES
('Tóxico', 'DANO_CONTINUO', 0.1, 'Causa daño fijo al equipo rival, reduciendo su vida en un 10% durante 4 turnos.'),

('Danza Espada', 'SUBIR_ATAQUE_PROPIO', 2, 'Duplica el ataque del usuario.'),
('Rizo defensa', 'SUBIR_DEFENSA_PROPIO', 2, 'Duplica la defensa del usuario.'),
('Látigo', 'BAJAR_DEFENSA_RIVAL', 1/1.25, 'Baja la defensa de los Pokémon del equipo rival en un 20%.'),
('Ojos llorosos', 'BAJAR_ATAQUE_RIVAL', 1/1.25, 'Baja el ataque de los Pokémon del equipo rival en un 20%.'),
('Recuperación', 'SUBIR_VIDA', 0.5, 'Recupera el 50% de la vida máxima del usuario.');


