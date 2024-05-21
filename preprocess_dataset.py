import pandas as pd

def modify_body_fat(row):
    return row.body_fat + (-8.6 if row.gender == 'F' else 0.0)

def round_numerical(row):
    for k, v in row.items():
        if k == 'gender' or k == 'class': continue
        row[k] = round(v, 2)

    return row

if __name__ == "__main__":
    df = pd.read_csv('bodyPerformance.csv')
    df = df.rename(columns={
        'height_cm': 'height',
        'weight_kg': 'weight',
        'body fat_%': 'body_fat',
        'gripForce': 'grip_force',
        'sit and bend forward_cm': 'sit_and_bend_forward',
        'sit-ups counts': 'sit_up_count',
        'broad jump_cm': 'broad_jump',
    })
    bmi = df.weight / (df.height * 0.01)
    df = df.assign(bmi=bmi)
    df['modified_body_fat'] = df.apply(modify_body_fat, axis=1)
    df = df.apply(round_numerical, axis=1)
    df = df.reindex(columns=[
        'age',
        'gender',
        'height',
        'weight',
        'bmi',
        'body_fat',
        'modified_body_fat',
        'diastolic',
        'systolic',
        'grip_force',
        'sit_and_bend_forward',
        'sit_up_count',
        'broad_jump',
    ])
    df.to_csv('dataset.csv', index=False)
