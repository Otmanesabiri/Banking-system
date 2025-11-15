# Guide des Virements Bancaires

## Types de Virements

### Virement Normal
Le virement normal est traité dans un délai de 24 à 48 heures ouvrées. Ce type de virement est idéal pour les paiements non urgents.

**Caractéristiques:**
- Délai: 24-48 heures
- Montant: illimité
- Coût: gratuit pour les virements SEPA
- Disponibilité: 24h/24, 7j/7

**Procédure:**
1. Sélectionner le bénéficiaire
2. Indiquer le montant
3. Ajouter une description (optionnel)
4. Valider le virement
5. Le virement sera exécuté le jour ouvré suivant

### Virement Instantané
Le virement instantané est exécuté en quelques secondes, 24h/24, y compris les weekends et jours fériés.

**Caractéristiques:**
- Délai: < 10 secondes
- Montant maximum: 15 000 €
- Coût: 0,80 € par virement
- Disponibilité: 24h/24, 365 jours par an
- Confirmation immédiate

**Avantages:**
- Réception immédiate des fonds
- Disponible en permanence
- Confirmation en temps réel
- Idéal pour les urgences

**Limitations:**
- Plafond de 15 000 € par virement
- Frais de 0,80 € par opération
- Le bénéficiaire doit être dans une banque compatible SEPA Instant

## Gestion des Bénéficiaires

### Créer un Bénéficiaire

**Informations requises:**
- Nom complet (pour personne physique) ou raison sociale (pour personne morale)
- Prénom (pour personne physique)
- RIB complet (IBAN + BIC)
- Type: Physique ou Morale

**Validation du RIB:**
Le système vérifie automatiquement:
- Format IBAN valide (34 caractères pour France)
- Clé de contrôle correcte
- Unicité du RIB

**Statut:**
- Actif: Le bénéficiaire peut recevoir des virements
- Inactif: Temporairement désactivé

### Modifier un Bénéficiaire

Vous pouvez modifier à tout moment:
- Le nom/prénom
- Le statut (actif/inactif)

**Important:** Le RIB ne peut pas être modifié pour des raisons de sécurité. Pour changer de RIB, créez un nouveau bénéficiaire.

### Supprimer un Bénéficiaire

La suppression d'un bénéficiaire:
- Est définitive
- N'affecte pas l'historique des virements passés
- Nécessite une confirmation

## Sécurité et Limites

### Plafonds de Virement

**Virements normaux:**
- Montant maximum par virement: 50 000 €
- Montant maximum journalier: 100 000 €
- Montant maximum mensuel: 500 000 €

**Virements instantanés:**
- Montant maximum par virement: 15 000 €
- Montant maximum journalier: 30 000 €
- Nombre maximum: 10 virements/jour

### Contrôles de Sécurité

**Vérifications automatiques:**
- Validation du RIB bénéficiaire
- Vérification de la disponibilité des fonds
- Détection de fraude (montants inhabituels)
- Conformité réglementaire (anti-blanchiment)

**Double authentification:**
Pour les virements > 5 000 €:
- Code SMS requis
- Confirmation par application mobile
- Délai de sécurité de 30 minutes

## Statuts des Virements

### EN_ATTENTE
Le virement est créé mais pas encore validé.
- Action possible: Modifier ou annuler
- Durée: Jusqu'à validation

### VALIDE
Le virement est validé et en attente d'exécution.
- Action possible: Annuler (jusqu'à J-1 pour virement normal)
- Durée: Jusqu'à date d'exécution

### EXECUTE
Le virement a été envoyé à la banque du bénéficiaire.
- Action possible: Aucune
- Le montant est débité

### REJETE
Le virement a été rejeté.
- Raisons possibles:
  - Solde insuffisant
  - RIB invalide
  - Dépassement de plafond
  - Compte bénéficiaire fermé

## Questions Fréquentes

### Combien de temps pour un virement international?
Les virements SEPA (zone euro) prennent 1-2 jours. Les virements internationaux hors SEPA peuvent prendre 3-5 jours ouvrés.

### Puis-je annuler un virement?
- Virement normal EN_ATTENTE ou VALIDE: Oui, jusqu'à la veille de l'exécution
- Virement instantané: Non, il est immédiat

### Que faire si le bénéficiaire n'a pas reçu le virement?
Vérifiez:
1. Le statut du virement (doit être EXECUTE)
2. Le RIB du bénéficiaire
3. Contactez votre conseiller après 48h

### Les virements sont-ils gratuits?
- Virements SEPA normaux: Gratuits
- Virements instantanés: 0,80 € par virement
- Virements internationaux: Variable selon destination

### Puis-je programmer un virement?
Oui, vous pouvez:
- Programmer une date d'exécution future
- Créer des virements permanents (mensuels)
- Définir des virements récurrents

## Support

Pour toute question ou problème:
- Hotline: 09 XX XX XX XX (du lundi au vendredi, 8h-18h)
- Email: support@banque.fr
- Chat en ligne: Disponible via l'application
- Agence: Prendre rendez-vous avec votre conseiller
